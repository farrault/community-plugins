package com.xebialabs.deployit.plugin.lock;

import static java.lang.String.format;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.Container;

public class RemoveLockStep implements DeploymentStep {

	private final Container container;
	private int order;

	public RemoveLockStep(int order, Container container) {
		this.container = container;
		this.order = order;
	}

	@Override
	public Result execute(DeploymentExecutionContext context) throws Exception {
		context.logOutput("Container " + container.getName() + " is locked, unlocking it for new deployments.");
		LockFileHelper.unlockContainer(container);
		return Result.Success;
	}

	@Override
	public String getDescription() {
		return format("Mark '%s' as available for deployments", container.getName());				
	}

	@Override
	public int getOrder() {
		return order;
	}

}
