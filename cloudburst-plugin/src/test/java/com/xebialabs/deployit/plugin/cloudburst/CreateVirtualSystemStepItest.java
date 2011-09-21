package com.xebialabs.deployit.plugin.cloudburst;

import static com.xebialabs.deployit.plugin.cloudburst.ReadVirtualSystemStep.VIRTUAL_SYSTEM_HOST_NAME_PREFIX;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.assertStepSucceeds;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.getDebugStepExecutionContext;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.plugin.cloudburst.CreateVirtualSystemStep;
import com.xebialabs.deployit.plugin.cloudburst.ReadVirtualSystemStep;

public class CreateVirtualSystemStepItest extends CloudBurstTestBase {

	@Test
	public void shouldCreateVirtualSystemAndReadInformationAboutIt() {
		Step createVirtualSystemStep = new CreateVirtualSystemStep(appliance, patternName, cloudGroupName, systemName, systemPassword);
		assertStepSucceeds(createVirtualSystemStep);

		Step readVirtualSystemStep = new ReadVirtualSystemStep(appliance, systemName);
		assertStepSucceeds(readVirtualSystemStep);

		String hostname = (String) getDebugStepExecutionContext().getAttribute(VIRTUAL_SYSTEM_HOST_NAME_PREFIX + systemName);
		assertThat(hostname, notNullValue());
		System.out.println("HOSTNAME = " + hostname);
	}

}
