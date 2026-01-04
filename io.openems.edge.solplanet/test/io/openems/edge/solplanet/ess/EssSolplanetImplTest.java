package io.openems.edge.solplanet.ess;

import org.junit.Test;

import io.openems.common.test.DummyConfigurationAdmin;
import io.openems.edge.common.test.ComponentTest;


public class EssSolplanetImplTest {
	
	@Test
	public void test() throws Exception {
		new ComponentTest(new EssSolplanetImpl()) //
			.addReference("cm", new DummyConfigurationAdmin()) //
	        .activate(EssSolplanetImplConfig.create() //
	            .setId("ess0") //
	            .setCoreId("core0")
	            .build()) //
		;		
	}
}
