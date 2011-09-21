package com.xebialabs.deployit.plugin.cloudburst;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.deployit.plugin.cloudburst.DiscoverWasUnmanagedServerOnVirtualSystemStep.WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX;

import java.util.List;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.ci.artifact.Ear;
import com.xebialabs.deployit.plugin.was.ci.WasTarget;
import com.xebialabs.deployit.plugin.was.ci.WasUnmanagedServer;
import com.xebialabs.deployit.plugin.was.step.WasDeployApplicationStep;
import com.xebialabs.deployit.plugin.was.step.WasStartApplicationStep;

@SuppressWarnings("serial")
public class DeployAndStartEarToDiscoveredWasUnmanagedServerOnVirtualSystemStep implements Step {

	private Ear ear;

	private String systemName;

	public DeployAndStartEarToDiscoveredWasUnmanagedServerOnVirtualSystemStep(Ear ear, String systemName) {
		this.ear = ear;
		this.systemName = systemName;
	}

	@Override
	public String getDescription() {
		return "Deploy " + ear + " on virtual system " + systemName + " and start it";
	}

	@Override
	public boolean execute(StepExecutionContext ctx) {
		WasUnmanagedServer server1 = (WasUnmanagedServer) ctx.getAttribute(WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX + systemName);
		if (server1 == null) {
			throw new IllegalStateException("Cannot find discovered WAS server for virtual system " + systemName + " in step execution context");
		}

		List<WasTarget> targets = newArrayList();
		targets.add(server1);
		Step deployStep = new WasDeployApplicationStep(server1, ear, targets, null, "default_host", null, 1, null, null, null, null, null);
		if (!deployStep.execute(ctx)) {
			return false;
		}

		Step startStep = new WasStartApplicationStep(server1, ear);
		return startStep.execute(ctx);
	}

}
