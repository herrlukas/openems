package io.openems.edge.solplanet.core;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.edge.solplanet.core.Config;

@SuppressWarnings("all")
public class CoreSolplanetImplConfig extends AbstractComponentConfig implements Config {

	public static class Builder {
		private String id;
		private String ip;
		private String sn;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}
		
		public Builder setIp(String ip) {
			this.ip = ip;
			return this;
		}
		
		public Builder setSn(String sn) {
			this.sn = sn;
			return this;
		}

		public CoreSolplanetImplConfig build() {
			return new CoreSolplanetImplConfig(this);
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

	private CoreSolplanetImplConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String ip() {
		return this.builder.ip;
	}

	@Override
	public String sn() {
		return this.builder.sn;
	}
}