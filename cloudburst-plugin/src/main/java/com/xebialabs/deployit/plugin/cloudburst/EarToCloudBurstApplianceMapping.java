package com.xebialabs.deployit.plugin.cloudburst;

import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.artifact.Ear;
import com.xebialabs.deployit.ci.artifact.mapping.DeployableArtifactMapping;

@SuppressWarnings("serial")
@ConfigurationItem
public class EarToCloudBurstApplianceMapping extends DeployableArtifactMapping<Ear, CloudBurstAppliance> {

	@ConfigurationItemProperty(required = true, description = "Name of the pattern to deploy")
	private String patternName;

	@ConfigurationItemProperty(required = true, description = "Name of the cloud group to deploy the pattern to")
	private String cloudGroupName;

	@ConfigurationItemProperty(required = true, description = "Name of the virtual system to be created")
	private String systemName;

	@ConfigurationItemProperty(required = true, password = true, description = "Administrative password that will be assigned to your operating system and your middleware")
	private String systemPassword;

	public String getPatternName() {
		return patternName;
	}

	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}

	public String getCloudGroupName() {
		return cloudGroupName;
	}

	public void setCloudGroupName(String cloudName) {
		this.cloudGroupName = cloudName;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getSystemPassword() {
		return systemPassword;
	}

	public void setSystemPassword(String systemPassword) {
		this.systemPassword = systemPassword;
	}

}
