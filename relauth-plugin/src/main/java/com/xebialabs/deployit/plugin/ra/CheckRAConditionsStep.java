package com.xebialabs.deployit.plugin.ra;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.ra.RAConditionVerifier.VerificationResult;

/**
 * Step that verifies that conditions listed in the environment's 'conditions' synthetic field (type: Set<String>)
 * are satisfied by the package being deployed. The conditions are literal references to synthetic properties on
 * the deployment package. Satisfying a condition depends on the property type:
 * - any object properties must be non-null
 * - boolean: the property is true
 * - String: the property is non-blank
 * - Collection properties: the collection is non-empty.
 */
@SuppressWarnings("serial")
public class CheckRAConditionsStep implements DeploymentStep {

	private final Environment environment;
	private final Version version;

	public CheckRAConditionsStep(Environment environment, Version version) {
		this.environment = environment;
		this.version = version;
	}

	@Override
	public String getDescription() {
		return "Verify release authorization for deployment to " + environment.getName();
	}

	@Override
	public com.xebialabs.deployit.plugin.api.execution.Step.Result execute(DeploymentExecutionContext ctx) throws Exception {
		RAConditionVerifier verifier = new RAConditionVerifier(version, environment);
		VerificationResult result = verifier.verify();
		
		if (result.isSuccess()) {
			ctx.logOutput(result.getLog());
			return Result.Success;
		} else {
			ctx.logError(result.getLog());
			return Result.Fail;
		}
	}

	@Override
	public int getOrder() {
		return -10;
	}

}
