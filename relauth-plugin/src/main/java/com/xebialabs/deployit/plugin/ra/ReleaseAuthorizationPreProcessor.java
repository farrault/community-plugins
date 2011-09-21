package com.xebialabs.deployit.plugin.ra;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;

/**
 * PreProcessor that adds a step to verify the release authorization checks to the beginning of the deployment plan.
 * The step is only added if the deployment is an initial deployment or upgrade and the target environment has
 * deployment conditions specified.
 */
public class ReleaseAuthorizationPreProcessor {

	@PrePlanProcessor
	public List<Step<DeploymentExecutionContext>> contributeCheckSteps(DeltaSpecification deltaSpecification) {
		DeployedApplication deployedApplication = deltaSpecification.getDeployedApplication();
		Environment environment = deployedApplication.getEnvironment();
		Version version = deployedApplication.getVersion();
		
		if (doConditionsApply(environment)) {
			// Only contribute step if we are deploying or upgrading
			boolean isCreateOrModify = false;
			
			List<Delta> deltas = deltaSpecification.getDeltas();
			for (Delta delta : deltas) {
				if (delta.getOperation() == Operation.CREATE || delta.getOperation() == Operation.MODIFY) {
					isCreateOrModify = true;
				}
			}
			
			if (isCreateOrModify) {
				return Collections.singletonList((Step<DeploymentExecutionContext>) new CheckRAConditionsStep(environment, version));
			}
		}
		
		return Collections.emptyList();
	}

	private boolean doConditionsApply(Environment environment) {
		if (environment.hasSyntheticProperty("conditions")) {
			Set<String> conditions = environment.getSyntheticProperty("conditions");
			return ! conditions.isEmpty();
		}
		return false;
	}
}
