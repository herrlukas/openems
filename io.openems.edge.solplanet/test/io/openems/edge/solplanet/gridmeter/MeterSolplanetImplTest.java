package io.openems.edge.solplanet.gridmeter;

import org.junit.Test;

import io.openems.edge.bridge.http.cycle.HttpBridgeCycleServiceDefinition;
import io.openems.edge.bridge.http.cycle.dummy.DummyCycleSubscriber;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.common.bridge.http.api.HttpError;
import io.openems.common.bridge.http.api.HttpResponse;
import io.openems.common.bridge.http.dummy.DummyBridgeHttpBundle;
import io.openems.edge.common.test.ComponentTest;

public class MeterSolplanetImplTest {

	private static final String SAMPLE_JSON = """
			{
				"flg":1,
				"tim":"",
				"pac":403,
				"itd":289,
				"otd":0,
				"iet":1346,
				"oet":16,
				"mod":22,
				"state_g100":0
			}
	""";
	
	@Test
	public void test() throws Exception {
		final var httpTestBundle = new DummyBridgeHttpBundle();
		final var dummyCycleSubscriber = new DummyCycleSubscriber();
		
		new ComponentTest(new MeterSolplanetImpl()) //
			.addReference("httpBridgeFactory", httpTestBundle.factory()) //
			.addReference("httpBridgeCycleServiceDefinition",
				new HttpBridgeCycleServiceDefinition(dummyCycleSubscriber)) //	
			.activate(MeterSolplanetImplConfig.create() //
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
				.output(ElectricityMeter.ChannelId.ACTIVE_POWER, 403)//
			);		
	}
	
	@Test 
	public void testWithNoData() throws Exception {
		final var httpTestBundle = new DummyBridgeHttpBundle();
		final var dummyCycleSubscriber = new DummyCycleSubscriber();
		
		new ComponentTest(new MeterSolplanetImpl()) //
			.addReference("httpBridgeFactory", httpTestBundle.factory()) //
			.addReference("httpBridgeCycleServiceDefinition",
				new HttpBridgeCycleServiceDefinition(dummyCycleSubscriber)) //	
			.activate(MeterSolplanetImplConfig.create() //
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
				.output(ElectricityMeter.ChannelId.ACTIVE_POWER, null)//
			);		
	}

}
