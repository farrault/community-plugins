package com.isoagroup.dpadmin.deployit.plugin;

import com.xebialabs.deployit.BaseConfigurationItem;
import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.Host;

@SuppressWarnings("serial")
@ConfigurationItem(description = "A DataPower appliance that is managed by DPAdmin")
public class DataPowerAppliance extends BaseConfigurationItem {

	@ConfigurationItemProperty(required = true, description = "Alias of the appliance as defined in the DPAdmin environment file")
	private String deviceAlias;

	@ConfigurationItemProperty(asContainment = true, description = "Host on which DPAdmin runs (default = localhost)")
	private Host dpadminHost;

	@ConfigurationItemProperty(required = true, description = "Path to the DPAdmin installation on the DPAdmin host")
	private String dpadminHomePath;

	public String getDeviceAlias() {
		return deviceAlias;
	}

	public void setDeviceAlias(String deviceAlias) {
		this.deviceAlias = deviceAlias;
	}

	public Host getDpadminHost() {
		return dpadminHost;
	}

	public void setDpadminHost(Host dpadminHost) {
		this.dpadminHost = dpadminHost;
	}

	public String getDpadminHomePath() {
		return dpadminHomePath;
	}

	public void setDpadminHomePath(String dpadminHomePath) {
		this.dpadminHomePath = dpadminHomePath;
	}

}
