package io.openems.edge.solplanet.ess;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.edge.solplanet.ess.Config;

@SuppressWarnings("all")
public class EssSolplanetImplConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public EssSolplanetImplConfig build() {
			return new EssSolplanetImplConfig(this);
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

	private EssSolplanetImplConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}
}