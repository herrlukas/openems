package io.openems.edge.solplanet.pvinverter;

import io.openems.common.test.AbstractComponentConfig;

@SuppressWarnings("all")
public class PvInverterSolplanetImplConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public PvInverterSolplanetImplConfig build() {
			return new PvInverterSolplanetImplConfig(this);
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

	private PvInverterSolplanetImplConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}
}