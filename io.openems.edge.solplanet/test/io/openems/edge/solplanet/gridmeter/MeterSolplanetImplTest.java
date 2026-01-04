package io.openems.edge.solplanet.gridmeter;

import org.junit.Test;

import io.openems.edge.common.test.ComponentTest;

public class MeterSolplanetImplTest {

	@Test
	public void test() throws Exception {	
		new ComponentTest(new MeterSolplanetImpl()) //
			.activate(MeterSolplanetImplConfig.create() //
					.setId("meter0") //
					.build()) //
		;		
	}
}
