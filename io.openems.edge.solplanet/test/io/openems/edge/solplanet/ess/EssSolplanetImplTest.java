package io.openems.edge.solplanet.ess;

import org.junit.Test;

import io.openems.edge.common.test.ComponentTest;


public class EssSolplanetImplTest {
	
	@Test
	public void test() throws Exception {
		new ComponentTest(new EssSolplanetImpl()) //
	        .activate(EssSolplanetImplConfig.create() //
	            .setId("ess0") //
	            .build()) //
		;		
	}
}
