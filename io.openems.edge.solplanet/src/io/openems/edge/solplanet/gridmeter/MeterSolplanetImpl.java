package io.openems.edge.solplanet.gridmeter;

import org.osgi.service.cm.ConfigurationAdmin;
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


import io.openems.common.types.MeterType;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.solplanet.core.CoreSolplanet;
import io.openems.edge.solplanet.core.SolplanetData;
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
	
	private final CalculateEnergyFromPower calculateProductionEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY);
	private final CalculateEnergyFromPower calculateConsumptionEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY);
	
	@Reference
	private ConfigurationAdmin cm;
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected CoreSolplanet core;
	
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
		// update filter for 'Core'
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "core", config.core_id())) {
			return;
		}
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
			this.updateChannels();
			this.calculateEnergy();
			break;
		}
	}
	
	private void updateChannels() {		
		SolplanetData spData = this.core.getSPData();
		this._setActivePower(spData.gridPower);
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