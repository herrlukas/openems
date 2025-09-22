package io.openems.pvinverter.solplanet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.openems.edge.bridge.http.api.HttpError;
import io.openems.edge.bridge.http.api.HttpResponse;
import io.openems.edge.bridge.http.dummy.DummyBridgeHttpBundle;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.common.test.ComponentTest;

public class PvInverterSolplanetImplTest {

	@Test
	public void test() throws Exception {
		final var sut = new PvInverterSolplanetImpl();
		final var httpTestBundle = new DummyBridgeHttpBundle();
		new ComponentTest(sut) //
			.addReference("httpBridgeFactory", httpTestBundle.factory()) //
			.activate(PvInverterSolplanetImplConfig.create() //
					.setId("meter0") //
					.setIp("127.0.0.1") //
					.setSN("xxx") //
					.build()) //
			.next(new TestCase("Successful read response") //
					.onBeforeProcessImage(() -> {
						httpTestBundle.forceNextSuccessfulResult(HttpResponse.ok("""
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
							"""));
						httpTestBundle.triggerNextCycle();
					}) //
					.onAfterProcessImage(() -> assertEquals("Production:585 W", sut.debugLog()))
					.output(ElectricityMeter.ChannelId.ACTIVE_POWER, 585))//
			.next(new TestCase("Invalid read response")
					.onBeforeProcessImage(() -> {
						httpTestBundle.forceNextFailedResult(HttpError.ResponseError.notFound());
						httpTestBundle.triggerNextCycle();
					}) //
					.onAfterProcessImage(() -> assertEquals("Production:UNDEFINED", sut.debugLog()))
					.output(ElectricityMeter.ChannelId.ACTIVE_POWER, null)) //
			.deactivate();
		
	}

}
