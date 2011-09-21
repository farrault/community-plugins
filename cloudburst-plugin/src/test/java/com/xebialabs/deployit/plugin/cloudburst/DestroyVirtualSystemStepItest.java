package com.xebialabs.deployit.plugin.cloudburst;

import static com.xebialabs.deployit.test.support.utils.ItestUtils.assertStepSucceeds;

import org.junit.Test;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.plugin.cloudburst.DestroyVirtualSystemStep;

public class DestroyVirtualSystemStepItest extends CloudBurstTestBase {

	@Test
	public void shouldDestroyVirtualSystem() {
		Step destroyVirtualSystemStep = new DestroyVirtualSystemStep(appliance, systemName);
		assertStepSucceeds(destroyVirtualSystemStep);
	}

}
