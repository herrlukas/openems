package io.openems.edge.meter.solplanet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.common.types.MeterType;
import io.openems.edge.bridge.http.api.HttpError;
import io.openems.edge.bridge.http.api.HttpResponse;
import io.openems.edge.bridge.http.dummy.DummyBridgeHttpBundle;
import io.openems.edge.common.test.ComponentTest;

public class MeterSolplanetImplTest {

	@Test
	public void test() throws Exception {
		final var sut = new MeterSolplanetImpl();
		final var httpTestBundle = new DummyBridgeHttpBundle();
		new ComponentTest(sut) //
				.addReference("httpBridgeFactory", httpTestBundle.factory()) //
				.activate(MyConfig.create() //
						.setId("meter0") //
						.setIp("127.0.0.1") //
						.setSN("xxx") //
						.setType(MeterType.GRID) //
						.build()) //
				.next(new TestCase("Successful read response") //
						.onBeforeProcessImage(() -> {
							httpTestBundle.forceNextSuccessfulResult(HttpResponse.ok("""
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
								"""));
							httpTestBundle.triggerNextCycle();
						}) //
						.onAfterProcessImage(() -> assertEquals("L:403 W", sut.debugLog()))
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER, 403))//
				.next(new TestCase("Invalid read response")
						.onBeforeProcessImage(() -> {
							httpTestBundle.forceNextFailedResult(HttpError.ResponseError.notFound());
							httpTestBundle.triggerNextCycle();
						}) //
						.onAfterProcessImage(() -> assertEquals("L:UNDEFINED", sut.debugLog()))
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER, null)) //
				.deactivate();
	}

}
