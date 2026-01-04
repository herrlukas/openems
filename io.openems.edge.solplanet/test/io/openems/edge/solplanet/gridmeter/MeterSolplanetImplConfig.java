package io.openems.edge.solplanet.gridmeter;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.common.types.MeterType;
import io.openems.edge.solplanet.gridmeter.Config;

@SuppressWarnings("all")
public class MeterSolplanetImplConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public MeterSolplanetImplConfig build() {
			return new MeterSolplanetImplConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 * 
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MeterSolplanetImplConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}
}