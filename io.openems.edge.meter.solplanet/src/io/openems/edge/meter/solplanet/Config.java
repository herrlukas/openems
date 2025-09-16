package io.openems.edge.meter.solplanet;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.common.types.MeterType;

@ObjectClassDefinition(//
		name = "Meter Solplanet", //
		description = "")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "meter0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Meter-Type", description = "Grid (=default), Production, Consumption" )
	MeterType type() default MeterType.GRID;
	
	@AttributeDefinition(name = "IP-Address", description = "The IP address of the AI-Dongel")
	String ip();
	
	@AttributeDefinition(name = "Serialnumber", description = "The serialnumber of the inverter")
	String sn();
	
	String webconsole_configurationFactory_nameHint() default "Meter Solplanet [{id}]";

}