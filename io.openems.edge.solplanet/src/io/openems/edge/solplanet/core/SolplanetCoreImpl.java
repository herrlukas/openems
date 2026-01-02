package io.openems.edge.solplanet.core;

import static io.openems.common.utils.JsonUtils.getAsInt;
import static io.openems.common.utils.JsonUtils.getAsJsonObject;
import static java.lang.Math.round;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import io.openems.common.bridge.http.api.BridgeHttp;
import io.openems.common.bridge.http.api.BridgeHttpFactory;
import io.openems.common.bridge.http.api.HttpError;
import io.openems.common.bridge.http.api.HttpResponse;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.bridge.http.cycle.HttpBridgeCycleServiceDefinition;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.solplanet.common.Helpers;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.solplanet.core", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class SolplanetCoreImpl extends AbstractOpenemsComponent implements SolplanetCore, OpenemsComponent, EventHandler {

	private Config config = null;

	private final Logger log = LoggerFactory.getLogger(SolplanetCoreImpl.class);
	
	@Reference
	private BridgeHttpFactory httpBridgeFactory;
	private BridgeHttp httpBridge;
	@Reference
	private HttpBridgeCycleServiceDefinition httpBridgeCycleServiceDefinition;
	
	private SolplanetData spData = null;
	
	public SolplanetCoreImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				SolplanetCore.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		
		this.httpBridge = this.httpBridgeFactory.get();
		
		if (this.isEnabled() == false) {
			return;
		}
		
		this.spData = new SolplanetData();
		
		Helpers.disableCertificateValidation();
		String url = Helpers.buildUrl(this.config.ip(), this.config.sn(), 4);
		final var cycleService = this.httpBridge.createService(this.httpBridgeCycleServiceDefinition);
		cycleService.subscribeJsonCycle(10, url, this::processHttpResult);
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
	}
	
	private void processHttpResult(HttpResponse<JsonElement> result, HttpError error) {	
		
		Integer pvPower = null, essPower = null, essSoc = null;
		
		if (error != null) {
			this.logDebug(this.log, error.getMessage());
		} else {
			try {
				var response = getAsJsonObject(result.data());
				pvPower = round(getAsInt(response, "ppv"));
				essPower = round(getAsInt(response, "pb"));
				essSoc = round(getAsInt(response, "soc"));
			} catch (OpenemsNamedException e) {
				this.logDebug(this.log, e.getMessage());
			}
		}
		
		this.spData.set(pvPower, essPower, essSoc);
	}
	
	public SolplanetData getSPData() {
		return this.spData;
	}

	@Override
	public String debugLog() {
		return "Production:" + this.spData.pvPower + "|SoC:" + this.spData.essSoc + "|L:" + this.spData.essPower;
	}
}
