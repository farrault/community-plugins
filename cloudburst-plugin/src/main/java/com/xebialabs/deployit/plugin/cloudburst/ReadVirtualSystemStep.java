package com.xebialabs.deployit.plugin.cloudburst;

import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.StepExecutionContextCallbackHandler;
import com.xebialabs.deployit.hostsession.PassThroughCapturingCommandExecutionCallbackHandler;

@SuppressWarnings("serial")
public class ReadVirtualSystemStep extends CloudBurstCliStep {

	private String systemName;

	public static final String VIRTUAL_SYSTEM_HOST_NAME_PREFIX = "virtualsystem.hostname.";

	public ReadVirtualSystemStep(CloudBurstAppliance appliance, String systemName) {
		super("Read information about virtual system " + systemName + " using CloudBurst appliance " + appliance, appliance,
		        "com/xebialabs/deployit/cloudburst/read-virtual-system.py", systemName);
		this.systemName = systemName;
	}

	@Override
	public boolean execute(StepExecutionContext context) {
		PassThroughCapturingCommandExecutionCallbackHandler handler = new PassThroughCapturingCommandExecutionCallbackHandler(new StepExecutionContextCallbackHandler(context));
		int res = execute(handler);
		if (res != 0) {
			return false;
		}

		for (String each : handler.getOutputLines()) {
			if (each.startsWith("hostname:")) {
				String hostname = each.substring("hostname:".length()).trim();
				context.logOutput("Hostname: " + hostname);
				context.setAttribute(VIRTUAL_SYSTEM_HOST_NAME_PREFIX + systemName, hostname);
				return true;
			}
		}

		context.logError("Cannot find hostname in output");
		return false;
	}
}
