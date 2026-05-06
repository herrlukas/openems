package io.openems.edge.solplanet.core;

import static io.openems.common.utils.JsonUtils.getAsInt;
import static io.openems.common.utils.JsonUtils.getAsJsonObject;
import static java.lang.Math.round;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.solplanet.core", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class CoreSolplanetImpl extends AbstractOpenemsComponent implements CoreSolplanet, OpenemsComponent, EventHandler {

	private Config config = null;

	private final Logger log = LoggerFactory.getLogger(CoreSolplanetImpl.class);
	
	@Reference
	private BridgeHttpFactory httpBridgeFactory;
	private BridgeHttp httpBridge;
	@Reference
	private HttpBridgeCycleServiceDefinition httpBridgeCycleServiceDefinition;
	
	private SolplanetData spData = null;
	
	public CoreSolplanetImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				CoreSolplanet.ChannelId.values() //
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
		
		this.disableCertificateValidation();
		String url = this.buildURL(4);
		final var cycleService = this.httpBridge.createService(this.httpBridgeCycleServiceDefinition);
		cycleService.subscribeJsonEveryCycle(url, this::processHttpResultInvEss);
		
		url = this.buildURL(3);
		cycleService.subscribeJsonEveryCycle(url, this::processHttpResultGrid);
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
	
	private void processHttpResultInvEss(HttpResponse<JsonElement> result, HttpError error) {	
		
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
		
		this.spData.pvPower = pvPower;
		this.spData.essPower = essPower;
		this.spData.essSoc = essSoc;
	}
	
	private void processHttpResultGrid(HttpResponse<JsonElement> result, HttpError error) {
		Integer gridPower = null;
		
		if (error != null) {
			this.logDebug(this.log, error.getMessage());
		} else {
			try {
				var response = getAsJsonObject(result.data());
				gridPower = round(getAsInt(response, "pac"));
			} catch (OpenemsNamedException e) {
				this.logDebug(this.log, e.getMessage());
			}
		}
		
		this.spData.gridPower = gridPower;
	}
	
	private String buildURL(Integer device) {
		String url = "https://" 
				+ this.config.ip() 
				+ ":443/getdevdata.cgi?device=" 
				+ device 
				+ "&sn=" 
				+ this.config.sn();
		return url;
	}
	
	private void disableCertificateValidation() {
	    try {
	        TrustManager[] trustAllCerts = new TrustManager[]{
	            new X509TrustManager() {
	                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
	            }
	        };

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Disable hostname verification
	        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public SolplanetData getSPData() {
		return this.spData;
	}

	@Override
	public String debugLog() {	
		return "Ess SoC:" 
				+ this.spData.essSoc 
				+ " %|L:" 
				+ this.spData.essPower 
				+ " W Grid:" 
				+ this.spData.gridPower 
				+ " W Production:" 
				+ this.spData.pvPower 
				+ " W";
	}
}
