package com.xebialabs.deployit.community.releaseauth.step;

import static com.xebialabs.deployit.community.releaseauth.ConditionVerifier.validateReleaseConditions;
import static java.lang.String.format;

import java.util.Set;

import com.xebialabs.deployit.community.releaseauth.ConditionVerifier.VerificationResult;
import com.xebialabs.deployit.community.releaseauth.ConditionVerifier.ViolatedCondition;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.Version;

@SuppressWarnings("serial")
public class CheckReleaseConditionsStep implements DeploymentStep {
	private final Set<String> conditions;
	private Version deploymentPackage;
	private final int order;

	public CheckReleaseConditionsStep(int order, Set<String> conditions, 
			Version deploymentPackage) {
		this.conditions = conditions;
		this.deploymentPackage = deploymentPackage;
		this.order = order;
	}

	@Override
	public Result execute(DeploymentExecutionContext ctx) throws Exception {
        VerificationResult result = validateReleaseConditions(conditions, deploymentPackage);
        
        ctx.logOutput("Verifying release conditions:");
        ctx.logOutput(buildValidatedConditionsMessage(result.getValidatedConditions()));
        
		if (result.failed()) {
			ctx.logError(buildViolatedConditionsMessage(result.getViolatedConditions()));
			return Result.Fail;
		} else {
			return Result.Success;
		}
	}

	private static String buildValidatedConditionsMessage(Set<String> validatedConditions) {
        StringBuilder message = new StringBuilder();
        for (String conditionName : validatedConditions) {
        	message.append(format("Condition '%s': OK%n", conditionName));
        }
		return message.toString();
	}

	private static String buildViolatedConditionsMessage(Set<ViolatedCondition<?>> violatedConditions) {
        StringBuilder message = new StringBuilder();
        for (ViolatedCondition<?> condition : violatedConditions) {
        	message.append(format("Condition '%s': FAIL <<< (expected %s but was '%s')%n", 
        			condition.name, condition.expectedValue, condition.actualValue));
        }
		return message.toString();
	}

	@Override
	public String getDescription() {
		return format("Verify release authorization for deployment of '%s'", deploymentPackage);
	}
	
	@Override
	public int getOrder() {
		return order;
	}
}
