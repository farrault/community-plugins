package com.xebialabs.deployit.plugin.ra;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;

/**
 * Step that verifies that conditions listed in the environment's 'conditions' synthetic field (type: Set<String>)
 * are satisfied by the package being deployed. The conditions are literal references to synthetic properties on
 * the deployment package. Satisfying a condition depends on the property type:
 * - any object properties must be non-null
 * - boolean: the property is true
 * - String: the property is non-blank
 * - Collection properties: the collection is non-empty.
 */
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

	@SuppressWarnings("rawtypes")
	@Override
	public com.xebialabs.deployit.plugin.api.execution.Step.Result execute(DeploymentExecutionContext ctx) throws Exception {
		Set<String> conditions = environment.getSyntheticProperty("conditions");
		
		for (String condition : conditions) {
			Object conditionProperty = version.getSyntheticProperty(condition);

			// Handles non-existent property as well as null object properties
			if (conditionProperty == null) {
				logFailedValidation(ctx, condition);
				return Result.Fail;
			}
			
			if (conditionProperty instanceof String && StringUtils.isBlank((String) conditionProperty)) {
				logFailedValidation(ctx, condition);
				return Result.Fail;				
			}
			
			if (conditionProperty instanceof Boolean && ! (Boolean) conditionProperty) {
				logFailedValidation(ctx, condition);
				return Result.Fail;				
			}

			if (conditionProperty instanceof Collection && ((Collection) conditionProperty).isEmpty()) {
				logFailedValidation(ctx, condition);
				return Result.Fail;				
			}

			ctx.logOutput("Release condition '" + condition + "': OK");
		}
		
		return Result.Success;
	}

	private void logFailedValidation(DeploymentExecutionContext ctx, String condition) {
		ctx.logError("Package " + version + " failed to meet release condition '" + condition + "'");
	}

	@Override
	public int getOrder() {
		return -10;
	}

}
