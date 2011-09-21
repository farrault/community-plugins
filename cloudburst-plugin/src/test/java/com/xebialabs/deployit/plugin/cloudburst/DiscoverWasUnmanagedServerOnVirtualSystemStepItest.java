package com.xebialabs.deployit.plugin.cloudburst;

import static com.xebialabs.deployit.plugin.cloudburst.DiscoverWasUnmanagedServerOnVirtualSystemStep.WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.assertStepSucceeds;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.getDebugStepExecutionContext;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.plugin.cloudburst.DiscoverWasUnmanagedServerOnVirtualSystemStep;
import com.xebialabs.deployit.plugin.cloudburst.ReadVirtualSystemStep;
import com.xebialabs.deployit.plugin.was.ci.WasUnmanagedServer;

public class DiscoverWasUnmanagedServerOnVirtualSystemStepItest extends CloudBurstTestBase {

	@Test
	public void shouldDiscoverWasUnmanagedServerOnVirtualSystem() {
		StepExecutionContext context = getDebugStepExecutionContext();

		context.setAttribute(ReadVirtualSystemStep.VIRTUAL_SYSTEM_HOST_NAME_PREFIX + systemName, hostname);

		Step step = new DiscoverWasUnmanagedServerOnVirtualSystemStep(systemName, systemPassword);
		assertStepSucceeds(step);

		WasUnmanagedServer discoveredServer1 = (WasUnmanagedServer) context.getAttribute(WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX + systemName);
		assertThat(discoveredServer1, notNullValue());
		assertThat(discoveredServer1.getMaxHeapSize(), equalTo(1256));
	}
}
