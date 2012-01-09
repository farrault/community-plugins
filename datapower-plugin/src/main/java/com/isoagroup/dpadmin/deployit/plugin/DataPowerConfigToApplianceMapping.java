package com.isoagroup.dpadmin.deployit.plugin;

import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.mapping.Mapping;

@SuppressWarnings("serial")
@ConfigurationItem
public class DataPowerConfigToApplianceMapping extends Mapping<DataPowerConfig, DataPowerAppliance> {

	@ConfigurationItemProperty(required = true, description = "Flow name, for reporting, also used as the script name (defaults to generated name)")
	private String flowName;


	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}


}
