package com.xebialabs.deployit.plugin.cloudburst;

import static com.xebialabs.deployit.plugin.cloudburst.DiscoverWasUnmanagedServerOnVirtualSystemStep.WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX;
import static com.xebialabs.deployit.plugin.cloudburst.ReadVirtualSystemStep.VIRTUAL_SYSTEM_HOST_NAME_PREFIX;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.assertStepSucceeds;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.getDebugStepExecutionContext;

import org.junit.Test;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.plugin.cloudburst.DeployAndStartEarToDiscoveredWasUnmanagedServerOnVirtualSystemStep;
import com.xebialabs.deployit.plugin.was.step.WasStopApplicationStep;
import com.xebialabs.deployit.plugin.was.step.WasUndeployApplicationStep;

public class DeployAndStartEarToExistingVirtualSystemStepItest extends CloudBurstTestBase {

	@Test
	public void shouldDeployEarToDiscoveredWasUnmanagedServerOnVirtualSystem() {
		StepExecutionContext context = getDebugStepExecutionContext();
		context.setAttribute(VIRTUAL_SYSTEM_HOST_NAME_PREFIX + systemName, hostname);
		context.setAttribute(WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX + systemName, server1);

		Step deployStep = new DeployAndStartEarToDiscoveredWasUnmanagedServerOnVirtualSystemStep(ear, systemName);
		assertStepSucceeds(deployStep);

		Step stopStep = new WasStopApplicationStep(server1, ear);
		assertStepSucceeds(stopStep);

		Step undeployStep = new WasUndeployApplicationStep(server1, ear);
		assertStepSucceeds(undeployStep);
	}
}
