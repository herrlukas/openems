package io.openems.pvinverter.solplanet;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "PV-Inverter Solplanet", //
		description = "")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "io.openems.pvinverter.solplanet0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "IP-Address", description = "The IP address of the AI-Dongel")
	String ip();
	
	@AttributeDefinition(name = "Serialnumber", description = "The serialnumber of the inverter")
	String sn();

	String webconsole_configurationFactory_nameHint() default "PV-Inverter Solplanet [{id}]";

}