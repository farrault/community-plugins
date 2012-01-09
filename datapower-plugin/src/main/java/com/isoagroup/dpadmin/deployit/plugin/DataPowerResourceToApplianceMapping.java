package com.isoagroup.dpadmin.deployit.plugin;

import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.mapping.Mapping;

@SuppressWarnings("serial")
@ConfigurationItem
public class DataPowerResourceToApplianceMapping extends Mapping<DataPowerResource, DataPowerAppliance> {

	@ConfigurationItemProperty(required = false, description = "Flow name, for reporting, also used as the script name (defaults to generated name)")
	private String flowName;

	@ConfigurationItemProperty(required = true, description = "Target directory into which to copy the resource")
	private String targetDirectory;

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(String targetFilename) {
		this.targetDirectory = targetFilename;
	}

}
