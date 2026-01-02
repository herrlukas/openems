package io.openems.edge.solplanet.pvinverter;

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

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.pvinverter.api.ManagedSymmetricPvInverter;
import io.openems.edge.solplanet.core.SolplanetCore;
import io.openems.edge.solplanet.core.SolplanetData;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateEnergyFromPower;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Solplanet.Pv-Inverter", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class PvInverterSolplanetImpl extends AbstractOpenemsComponent 
	implements PvInverterSolplanet, OpenemsComponent, ManagedSymmetricPvInverter, ElectricityMeter, TimedataProvider, EventHandler {
	
	private final CalculateEnergyFromPower calculateActualEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY);
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected SolplanetCore core;
	
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
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			System.out.println("Event");
			this.updateChannels();
			this.calculateEnergy();
			break;
		}
	}
	
	private void updateChannels() {		
		
		if (this.core == null) {
			System.out.println("Core: Null");
		}
		
		SolplanetData spData = this.core.getSPData();
		
		System.out.println(spData.pvPower);
		
		this._setActivePower(spData.pvPower);
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
