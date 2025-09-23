package io.openems.edge.solplanet.gridmeter;

import static io.openems.common.utils.JsonUtils.getAsJsonObject;
import static io.openems.common.utils.JsonUtils.getAsInt;
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
import io.openems.common.types.MeterType;
import io.openems.edge.bridge.http.api.BridgeHttp;
import io.openems.edge.bridge.http.api.BridgeHttpFactory;
import io.openems.edge.bridge.http.api.HttpError;
import io.openems.edge.bridge.http.api.HttpResponse;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateEnergyFromPower;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Solplanet.Gridmeter", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class MeterSolplanetImpl extends AbstractOpenemsComponent 
	implements MeterSolplanet, ElectricityMeter, OpenemsComponent, TimedataProvider, EventHandler {

	private Config config = null;

	private final Logger log = LoggerFactory.getLogger(MeterSolplanetImpl.class);
	
	private final CalculateEnergyFromPower calculateProductionEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY);
	private final CalculateEnergyFromPower calculateConsumptionEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY);
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference()
	private BridgeHttpFactory httpBridgeFactory;
	private BridgeHttp httpBridge;

	
	public MeterSolplanetImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				MeterSolplanet.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		
		this.config = config;
		this.httpBridge = this.httpBridgeFactory.get();
		
		if (this.isEnabled()) {
			String url = "http://" + this.config.ip() + ":8484/getdevdata.cgi?device=3&sn=" + this.config.sn();
			this.httpBridge.subscribeJsonEveryCycle(url, this::processHttpResult);
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
				activePower = round(getAsInt(response, "pac"));
			} catch (OpenemsNamedException e) {
				this.logDebug(this.log, e.getMessage());
			}
		}

		this._setActivePower(activePower);
	}
	
	private void calculateEnergy() {
		 Integer activePower = this.getActivePower().orElse(null);
		 
		 if (activePower == null) {
			 // Not available
			 this.calculateProductionEnergy.update(null);
			 this.calculateConsumptionEnergy.update(null);
		 } else if (activePower > 0) {
			 // Buy-From-Grid
			 this.calculateProductionEnergy.update(activePower);
			 this.calculateConsumptionEnergy.update(0);
		 } else {
			 // Sell-To-Grid 
			 this.calculateProductionEnergy.update(0);
			 this.calculateConsumptionEnergy.update(-activePower);
		 }
	}
	
	@Override
	public String debugLog() {
		return "L:" + this.getActivePower().asString();
	}
	
	@Override 
	public MeterType getMeterType() {
		return MeterType.GRID;
	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}