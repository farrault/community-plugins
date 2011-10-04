package com.xebialabs.deployit.plugin.ra;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.api.deployment.planning.PostPlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.ra.RAConditionVerifier.VerificationResult;

/**
 * PostProcessor that instantly checks the conditions specified for the deployment, if any.
 * 
 * This implementation provides fast feedback of release authorization violations.
 */
public class ReleaseAuthorizationPostProcessor {

	@PostPlanProcessor
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
				System.out.println("Verifying release auth in post processor");
				RAConditionVerifier verifier = new RAConditionVerifier(version, environment);
				VerificationResult result = verifier.verify();
				if (! result.isSuccess()) {
					throw new RuntimeException(result.getLog());
				}
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
