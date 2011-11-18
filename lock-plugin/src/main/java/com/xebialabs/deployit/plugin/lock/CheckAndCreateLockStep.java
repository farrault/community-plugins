package com.xebialabs.deployit.plugin.lock;

import static java.lang.String.format;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.Container;

public class CheckAndCreateLockStep implements DeploymentStep {

	private final int order;
	private final Container container;
	
	public CheckAndCreateLockStep(int order, Container container) {
		this.order = order;
		this.container = container;
	}

	@Override
	public Result execute(DeploymentExecutionContext context) throws Exception {
		if (LockFileHelper.isLocked(container)) {
			context.logError("Container " + container.getName() + " is locked. A different deployment may already be in progress.");
			return Result.Fail;
		}

		context.logOutput("Container " + container.getName() + " is available, locking it for this deployment.");

		LockFileHelper.lockContainer(container);
		
		return Result.Success;
	}

	@Override
	public String getDescription() {
		return format("Check for deployments already in progress on '%s'", container.getName());				
    }

	@Override
	public int getOrder() {
		return order;
	}

}
