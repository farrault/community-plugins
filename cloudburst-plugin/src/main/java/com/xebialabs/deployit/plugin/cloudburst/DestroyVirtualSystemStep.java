package com.xebialabs.deployit.plugin.cloudburst;


@SuppressWarnings("serial")
public class DestroyVirtualSystemStep extends CloudBurstCliStep {

	public DestroyVirtualSystemStep(CloudBurstAppliance appliance, String systemName) {
		super("Destroy virtual system " + systemName + " using CloudBurst appliance " + appliance, appliance,
		        "com/xebialabs/deployit/cloudburst/destroy-virtual-system.py", systemName);
	}

}
