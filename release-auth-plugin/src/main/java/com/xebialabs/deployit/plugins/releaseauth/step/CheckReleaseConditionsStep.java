package com.xebialabs.deployit.plugins.releaseauth.step;

import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.deployit.plugins.releaseauth.planning.CheckReleaseConditionsAreMet.ENV_RELEASE_CONDITIONS_PROPERTY;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;

import java.util.Set;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;

@SuppressWarnings("serial")
public class CheckReleaseConditionsStep implements DeploymentStep {

	private final DeployedApplication deployedApplication;
	private final int order;

	public CheckReleaseConditionsStep(int order, DeployedApplication deployedApplication) {
		this.deployedApplication = deployedApplication;
		this.order = order;
	}

	@Override
	public Result execute(DeploymentExecutionContext ctx) throws Exception {
        Set<String> violatedConditions = validateReleaseConditions(deployedApplication);
        
		if (violatedConditions.isEmpty()) {
			ctx.logOutput("All release conditions verified");
			return Result.Success;
		} else {
			ctx.logError(buildErrorMessage(deployedApplication, violatedConditions));
			return Result.Fail;
		}
	}

    protected static Set<String> validateReleaseConditions(DeployedApplication deployedApplication) {
        Set<String> conditions = deployedApplication.getEnvironment().getProperty(ENV_RELEASE_CONDITIONS_PROPERTY);
        if ((conditions == null) || (conditions.isEmpty())) {
            return newHashSet();
        }
        
        Set<String> violatedConditions = newHashSet();
        for (String conditionName : conditions) {
            if (!TRUE.equals(deployedApplication.getVersion().getProperty(conditionName))) {
                violatedConditions.add(conditionName);
            }
        }
        return violatedConditions;
    }
    
    private static String buildErrorMessage(DeployedApplication deployedApplication,
            Set<String> violatedConditions) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Cannot deploy '").append(deployedApplication.getName())
        .append("' (version ").append(deployedApplication.getVersion().getVersion())
        .append(") to '").append(deployedApplication.getEnvironment().getName())
        .append("' as the following release conditions are not met:");
        for (String violatedConditionName : violatedConditions) {
            errorMessage.append("\n- '").append(violatedConditionName).append("'");
        }
        return errorMessage.toString();
    }
    
	@Override
	public String getDescription() {
		return format("Verify release authorization for deployment of '%s' to '%s'",
				deployedApplication.getVersion(), deployedApplication.getEnvironment());
	}
	
	@Override
	public int getOrder() {
		return order;
	}
}
