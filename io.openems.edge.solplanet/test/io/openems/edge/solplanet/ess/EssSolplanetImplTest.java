package io.openems.edge.solplanet.ess;

import org.junit.Test;

import io.openems.edge.bridge.http.cycle.HttpBridgeCycleServiceDefinition;
import io.openems.edge.bridge.http.cycle.dummy.DummyCycleSubscriber;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.common.bridge.http.api.HttpError;
import io.openems.common.bridge.http.api.HttpResponse;
import io.openems.common.bridge.http.dummy.DummyBridgeHttpBundle;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.ess.api.SymmetricEss;


public class EssSolplanetImplTest {

	private static final String SAMPLE_JSON = """
			{
			    "flg": 1,
			    "tim": "20250820192236",
			    "ppv": 585,
			    "etdpv": 79,
			    "etopv": 84,
			    "cst": 10,
			    "bst": 2,
			    "eb1": 65535,
			    "wb1": 65535,
			    "vb": 16090,
			    "cb": -24,
			    "pb": -386,
			    "tb": 258,
			    "soc": 96,
			    "soh": 100,
			    "cli": 150,
			    "clo": 300,
			    "ebi": 47,
			    "ebo": 4,
			    "eaci": 0,
			    "eaco": 0,
			    "vesp": 2355,
			    "cesp": 0,
			    "fesp": 4993,
			    "pesp": 0,
			    "rpesp": 0,
			    "etdesp": 1,
			    "etoesp": 1,
			    "vl1esp": 2348,
			    "il1esp": 0,
			    "pac1esp": 0,
			    "qac1esp": 0,
			    "vl2esp": 2365,
			    "il2esp": 0,
			    "pac2esp": 0,
			    "qac2esp": 0,
			    "vl3esp": 2351,
			    "il3esp": 0,
			    "pac3esp": 0,
			    "qac3esp": 0
			}
	""";
	
	@Test
	public void test() throws Exception {
		final var httpTestBundle = new DummyBridgeHttpBundle();
		final var dummyCycleSubscriber = new DummyCycleSubscriber();
		
		new ComponentTest(new EssSolplanetImpl()) //
			.addReference("httpBridgeFactory", httpTestBundle.factory()) //
			.addReference("httpBridgeCycleServiceDefinition",
				new HttpBridgeCycleServiceDefinition(dummyCycleSubscriber)) //	
			.activate(EssSolplanetImplConfig.create() //
					.setId("meter0") //
					.setIp("127.0.0.1") //
					.setSN("xxx") //
					.build()) //
			.next(new TestCase() //
					.onBeforeProcessImage(() -> {
						httpTestBundle.forceNextSuccessfulResult(HttpResponse.ok(SAMPLE_JSON));
						for (int i = 0; i < 10; i++) {
					        dummyCycleSubscriber.triggerNextCycle();
					    }
					})) //
			.next(new TestCase()//
					.output(SymmetricEss.ChannelId.ACTIVE_POWER, -386)//
					.output(SymmetricEss.ChannelId.SOC, 96) //
			);		
	}
	
	@Test 
	public void testWithNoData() throws Exception {
		final var httpTestBundle = new DummyBridgeHttpBundle();
		final var dummyCycleSubscriber = new DummyCycleSubscriber();
		
		new ComponentTest(new EssSolplanetImpl()) //
			.addReference("httpBridgeFactory", httpTestBundle.factory()) //
			.addReference("httpBridgeCycleServiceDefinition",
				new HttpBridgeCycleServiceDefinition(dummyCycleSubscriber)) //	
			.activate(EssSolplanetImplConfig.create() //
					.setId("meter0") //
					.setIp("127.0.0.1") //
					.setSN("xxx") //
					.build()) //
			.next(new TestCase() //
					.onBeforeProcessImage(() -> {
						httpTestBundle.forceNextFailedResult(HttpError.ResponseError.notFound());
						dummyCycleSubscriber.triggerNextCycle();
					})) //
			.next(new TestCase()//
					.output(SymmetricEss.ChannelId.ACTIVE_POWER, null)//
					.output(SymmetricEss.ChannelId.SOC, null) //
			);		
	}
}
