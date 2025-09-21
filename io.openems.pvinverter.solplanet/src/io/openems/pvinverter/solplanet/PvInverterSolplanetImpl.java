package io.openems.pvinverter.solplanet;

import static io.openems.common.utils.JsonUtils.getAsInt;
import static io.openems.common.utils.JsonUtils.getAsJsonObject;
import static java.lang.Math.round;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.bridge.http.api.BridgeHttp;
import io.openems.edge.bridge.http.api.BridgeHttpFactory;
import io.openems.edge.bridge.http.api.HttpError;
import io.openems.edge.bridge.http.api.HttpResponse;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.pvinverter.api.ManagedSymmetricPvInverter;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateEnergyFromPower;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Pv-Inverter.Solplanet", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class PvInverterSolplanetImpl extends AbstractOpenemsComponent 
	implements PvInverterSolplanet, OpenemsComponent, ManagedSymmetricPvInverter, ElectricityMeter, TimedataProvider, EventHandler {

	private Config config = null;

	private final Logger log = LoggerFactory.getLogger(PvInverterSolplanetImpl.class);
	
	private final CalculateEnergyFromPower calculateActualEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY);
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference()
	private BridgeHttpFactory httpBridgeFactory;
	private BridgeHttp httpBridge;
	
	public PvInverterSolplanetImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ManagedSymmetricPvInverter.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				PvInverterSolplanet.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		
		this.httpBridge = this.httpBridgeFactory.get();
		
		if (this.isEnabled()) {
			String url = "http://" + this.config.ip() + ":8484/getdevdata.cgi?device=4&sn=" + this.config.sn();
			this.httpBridge.subscribeJsonEveryCycle(url , this::processHttpResult);
		}		
	}

	@Deactivate
	protected void deactivate() {
		this.httpBridgeFactory.unget(this.httpBridge);
		this.httpBridge = null;
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			this.calculateEnergy();
			break;
		}
	}
	
	private void processHttpResult(HttpResponse<JsonElement> result, HttpError error) {
		Integer activePower = null;
		
		if (error != null) {
			this.logDebug(this.log, error.getMessage());
		} else {
			try {
				var response = getAsJsonObject(result.data());
				activePower = round(getAsInt(response, "ppv"));
			} catch (OpenemsNamedException e) {
				this.logDebug(this.log, e.getMessage());
			}
		}
		
		this._setActivePower(activePower);
	}
	
	private void calculateEnergy() {
		var activePower = this.getActivePower().orElse(null);
		
		if (activePower == null) {
			this.calculateActualEnergy.update(null);
		} else if (activePower > 0) {
			this.calculateActualEnergy.update(activePower);
		} else {
			this.calculateActualEnergy.update(0);
		}
	}

	@Override
	public String debugLog() {
		return "Production:" + this.getActivePower();
	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}
