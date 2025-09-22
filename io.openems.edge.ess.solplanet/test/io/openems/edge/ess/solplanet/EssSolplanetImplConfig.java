package io.openems.edge.ess.solplanet;

import io.openems.common.test.AbstractComponentConfig;

@SuppressWarnings("all")
public class EssSolplanetImplConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
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
		
		public Builder setSN(String sn) {
			this.sn = sn;
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

	@Override
	public String ip() {
		return this.builder.ip;
	}

	@Override
	public String sn() {
		return this.builder.sn;
	}


}