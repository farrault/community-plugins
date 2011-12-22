package com.xebialabs.deployit.plugin.lock;

import static java.lang.String.format;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.execution.ExecutionContextListener;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Environment;

@SuppressWarnings("serial")
public class CheckAndCreateLockStep implements DeploymentStep {

	private final int order;
	private final ConfigurationItem[] cisToBeLocked;
	private static final String CI_LOCK_CLEANER = "ciLockCleaner";

	public CheckAndCreateLockStep(int order, ConfigurationItem... cisToBeLocked) {
		this.order = order;
		this.cisToBeLocked = cisToBeLocked;
	}

	@Override
	public Result execute(DeploymentExecutionContext context) throws Exception {
		Result executionResult = Result.Success;
		configureLockCleaner(context, cisToBeLocked);
		for (ConfigurationItem each : cisToBeLocked) {
			String targetType = (each instanceof Environment) ? "Environment " : "Container ";
			if (LockFileHelper.isLocked(each)) {
				context.logError(targetType + each.getName() + " is locked. A different deployment may already be in progress.");
				executionResult = Result.Fail;
			}

			context.logOutput(targetType + each.getName() + " is available, locking it for this deployment.");
			LockFileHelper.lock(each);

		}

		return executionResult;
	}

	@Override
	public String getDescription() {
		return format("Check for deployments already in progress");
	}

	@Override
	public int getOrder() {
		return order;
	}
	
	private void configureLockCleaner(DeploymentExecutionContext context, ConfigurationItem... cisToBeLocked) {
		LockCleaner lockCleaner = (LockCleaner) context.getAttribute(CI_LOCK_CLEANER);
		if(lockCleaner == null) {
			lockCleaner = new LockCleaner(cisToBeLocked);
			context.setAttribute(CI_LOCK_CLEANER, lockCleaner);
		}
	}
	
	private class LockCleaner implements ExecutionContextListener {
		private final ConfigurationItem[] cisToBeLocked;
		
		public LockCleaner(ConfigurationItem... cisToBeLocked) {
			this.cisToBeLocked = cisToBeLocked;
		}
		
		@Override
        public void contextDestroyed() {
	       for(ConfigurationItem ci : cisToBeLocked) {
	    	   LockFileHelper.unlock(ci);
	       }
        }
		
	}
	

}
