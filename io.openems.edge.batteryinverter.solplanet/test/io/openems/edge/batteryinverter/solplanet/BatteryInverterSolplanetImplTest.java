package io.openems.edge.batteryinverter.solplanet;

import org.junit.Test;

import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;

public class BatteryInverterSolplanetImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new BatteryInverterSolplanetImpl()) //
				.activate(MyConfig.create() //
						.setId("component0") //
						.build()) //
				.next(new TestCase()) //
				.deactivate();
	}

}
