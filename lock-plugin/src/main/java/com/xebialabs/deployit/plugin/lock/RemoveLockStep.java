package com.xebialabs.deployit.plugin.lock;

import static java.lang.String.format;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Environment;

@SuppressWarnings("serial")
public class RemoveLockStep implements DeploymentStep {

	private final ConfigurationItem[] cisToBeUnlocked;
	private int order;

	public RemoveLockStep(int order, ConfigurationItem... cisToBeUnlocked) {
		this.cisToBeUnlocked = cisToBeUnlocked;
		this.order = order;
	}

	@Override
	public Result execute(DeploymentExecutionContext context) throws Exception {
		for(ConfigurationItem each : cisToBeUnlocked) {
			String targetType = (each instanceof Environment) ? "Environment " : "Container ";
			context.logOutput(targetType + each.getName() + " is locked, unlocking it for new deployments.");
			LockFileHelper.unlock(each);
		}
		return Result.Success;
	}

	@Override
	public String getDescription() {
		String targetType = (cisToBeUnlocked.length == 1 && cisToBeUnlocked[0] instanceof Environment) ? "Environment " : "Containers ";
		return format("Mark the %s as available for deployments", targetType);				
	}

	@Override
	public int getOrder() {
		return order;
	}

}
