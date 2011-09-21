package com.xebialabs.deployit.plugin.cloudburst;

import com.xebialabs.deployit.BaseConfigurationItem;
import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.Host;

@SuppressWarnings("serial")
@ConfigurationItem
public class CloudBurstAppliance extends BaseConfigurationItem {

	@ConfigurationItemProperty(required = true, asContainment = true, description = "Host on which the CloudBurst CLI has been installed")
	private Host cliHost;

	@ConfigurationItemProperty(required = true, label = "CLI Home", description = "Path to the CloudBurst CLI installation")
	private String cliHome;

	@ConfigurationItemProperty(required = true, description = "Adress of the WebSphere CloudBurst appliance")
	private String address;

	@ConfigurationItemProperty(required = true, description = "Administrative username for the CloudBurst appliance")
	private String username;

	@ConfigurationItemProperty(required = true, password = true, description = "Administrative password for the CloudBurst appliance")
	private String password;

	public Host getCliHost() {
		return cliHost;
	}

	public void setCliHost(Host cliHost) {
		this.cliHost = cliHost;
	}

	public String getCliHome() {
		return cliHome;
	}

	public void setCliHome(String cliHome) {
		this.cliHome = cliHome;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
