package com.xebialabs.deployit.plugin.cloudburst;


@SuppressWarnings("serial")
public class CreateVirtualSystemStep extends CloudBurstCliStep {

	public CreateVirtualSystemStep(CloudBurstAppliance appliance, String patternName, String cloudName, String systemName, String systemPassword) {
		super("Deploy pattern " + patternName + " to cloud " + cloudName + " with system name " + systemName + " using CloudBurst appliance " + appliance,
		        appliance, "com/xebialabs/deployit/cloudburst/create-virtual-system.py", patternName, cloudName, systemName, systemPassword);
	}

}
