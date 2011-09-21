package com.xebialabs.deployit.plugin.cloudburst;

import static com.xebialabs.deployit.test.support.utils.ItestUtils.assertStepSucceeds;

import org.junit.Test;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.plugin.cloudburst.CreateVirtualSystemStep;
import com.xebialabs.deployit.plugin.cloudburst.DeployAndStartEarToDiscoveredWasUnmanagedServerOnVirtualSystemStep;
import com.xebialabs.deployit.plugin.cloudburst.DiscoverWasUnmanagedServerOnVirtualSystemStep;
import com.xebialabs.deployit.plugin.cloudburst.ReadVirtualSystemStep;
import com.xebialabs.deployit.plugin.was.step.WasStopApplicationStep;
import com.xebialabs.deployit.plugin.was.step.WasUndeployApplicationStep;

public class DeployAndStartEarToNewVirtualSystemItest extends CloudBurstTestBase {

	@Test
	public void shouldCreateVirtualSystemAndDiscoverWasUnmanagedServerAndDeployEar() {
		Step createVirtualSystemStep = new CreateVirtualSystemStep(appliance, patternName, cloudGroupName, systemName, systemPassword);
		assertStepSucceeds(createVirtualSystemStep);

		Step readVirtualSystemStep = new ReadVirtualSystemStep(appliance, systemName);
		assertStepSucceeds(readVirtualSystemStep);

		Step discoverStep = new DiscoverWasUnmanagedServerOnVirtualSystemStep(systemName, systemPassword);
		assertStepSucceeds(discoverStep);

		Step deployStep = new DeployAndStartEarToDiscoveredWasUnmanagedServerOnVirtualSystemStep(ear, systemName);
		assertStepSucceeds(deployStep);

		Step stopStep = new WasStopApplicationStep(server1, ear);
		assertStepSucceeds(stopStep);

		Step undeployStep = new WasUndeployApplicationStep(server1, ear);
		assertStepSucceeds(undeployStep);
	}
}
