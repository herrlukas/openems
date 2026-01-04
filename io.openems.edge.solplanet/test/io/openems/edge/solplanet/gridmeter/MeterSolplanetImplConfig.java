package io.openems.edge.solplanet.gridmeter;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.common.types.MeterType;
import io.openems.common.utils.ConfigUtils;
import io.openems.edge.solplanet.gridmeter.Config;

@SuppressWarnings("all")
public class MeterSolplanetImplConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
		private String coreId;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}
		
		public Builder setCoreId(String coreId) {
			this.coreId = coreId;
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
	
	@Override
	public String core_id() {
		return this.builder.coreId;
	}

	@Override
	public String core_target() {
		return ConfigUtils.generateReferenceTargetFilter(this.id(), this.core_id());
	}
}