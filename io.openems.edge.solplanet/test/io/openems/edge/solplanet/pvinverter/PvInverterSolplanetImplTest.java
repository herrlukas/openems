package io.openems.edge.solplanet.pvinverter;

import org.junit.Test;

import io.openems.edge.common.test.ComponentTest;

public class PvInverterSolplanetImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new PvInverterSolplanetImpl()) //
			.activate(PvInverterSolplanetImplConfig.create() //
					.setId("meter0") //
					.build()) //
		;		
	}
}
