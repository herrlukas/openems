package io.openems.edge.batteryinverter.solplanet;

import io.openems.common.test.AbstractComponentConfig;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

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

		public Builder setIP(String ip) {
			this.ip = ip;
			return this;
		}
		
		public Builder setSN(String sn) {
			this.ip = sn;
			return this;
		}

		public MyConfig build() {
			return new MyConfig(this);
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

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String ip() {
		return this.ip();
	}

	@Override
	public String sn() {
		return this.sn();
	}

//	@Override
//	public String setting0() {
//		return this.builder.setting0;
//	}

}