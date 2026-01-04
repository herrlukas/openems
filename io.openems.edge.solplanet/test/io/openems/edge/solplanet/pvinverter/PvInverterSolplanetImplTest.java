package io.openems.edge.solplanet.pvinverter;

import org.junit.Test;

import io.openems.common.test.DummyConfigurationAdmin;
import io.openems.edge.common.test.ComponentTest;

public class PvInverterSolplanetImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new PvInverterSolplanetImpl()) //
			.addReference("cm", new DummyConfigurationAdmin()) //
			.activate(PvInverterSolplanetImplConfig.create() //
					.setId("meter0") //
					.setCoreId("core0") //
					.build()) //
		;		
	}
}
