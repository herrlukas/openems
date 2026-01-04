package io.openems.edge.solplanet.core;

import org.junit.Test;

import io.openems.common.bridge.http.dummy.DummyBridgeHttpBundle;
import io.openems.edge.bridge.http.cycle.HttpBridgeCycleServiceDefinition;
import io.openems.edge.bridge.http.cycle.dummy.DummyCycleSubscriber;
import io.openems.edge.common.test.ComponentTest;

public class CoreSolplanetImplTest {

	@Test
	public void test() throws Exception {
		final var httpTestBundle = new DummyBridgeHttpBundle();
		final var dummyCycleSubscriber = new DummyCycleSubscriber();
				
		new ComponentTest(new CoreSolplanetImpl()) //
			.addReference("httpBridgeFactory", httpTestBundle.factory()) //
			.addReference("httpBridgeCycleServiceDefinition",
					new HttpBridgeCycleServiceDefinition(dummyCycleSubscriber)) //	
			.activate(CoreSolplanetImplConfig.create() //
					.setId("core0") //
					.setIp("127.0.0.1") //
					.setSn("xxx") //
					.build()) //
		;
	}

}