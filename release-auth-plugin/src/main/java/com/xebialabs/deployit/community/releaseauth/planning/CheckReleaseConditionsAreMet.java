/*
 * @(#)CheckRequiredChangeRequest.java     26 Aug 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.community.releaseauth.planning;

import static com.xebialabs.deployit.community.releaseauth.ConditionVerifier.validateReleaseConditions;
import static java.lang.Boolean.TRUE;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.xebialabs.deployit.community.releaseauth.ConditionVerifier.VerificationResult;
import com.xebialabs.deployit.community.releaseauth.ConditionVerifier.ViolatedCondition;
import com.xebialabs.deployit.community.releaseauth.step.CheckReleaseConditionsStep;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.PostPlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;

public class CheckReleaseConditionsAreMet {
    public static final String ENV_RELEASE_CONDITIONS_PROPERTY = "releaseConditions";
    private static final String ENV_RECHECK_CONDITIONS_PROPERTY = "recheckConditionsAtDeploymentTime";
    private static final String ENV_RECHECK_CONDITIONS_ORDER_PROPERTY = "recheckConditionsAtDeploymentTimeOrder";

    private static final List<DeploymentStep> NO_STEPS = ImmutableList.of();
    
    // allow other plugins to dynamically set/modify release conditions before validation
    @PostPlanProcessor
    public static List<DeploymentStep> validate(DeltaSpecification spec) {
        DeployedApplication deployedApplication = spec.getDeployedApplication();
        Set<String> conditions = getReleaseConditions(deployedApplication);
        
        if (conditions.isEmpty()) {
        	return NO_STEPS;
        }
        
        VerificationResult result = validateReleaseConditions(conditions, 
        		deployedApplication.getVersion());
        if (result.failed()) {
            throw new IllegalArgumentException(buildErrorMessage(
                    deployedApplication, result.getViolatedConditions()));
        }
        
        Builder<DeploymentStep> deploymentSteps = ImmutableList.builder();
        Environment environment = deployedApplication.getEnvironment();
        if (TRUE.equals(environment.getProperty(ENV_RECHECK_CONDITIONS_PROPERTY))) {
        	deploymentSteps.add(new CheckReleaseConditionsStep(
        				environment.<Integer>getProperty(ENV_RECHECK_CONDITIONS_ORDER_PROPERTY), 
        				conditions, deployedApplication.getVersion()));
        }
        return deploymentSteps.build();
    }
    
    private static Set<String> getReleaseConditions(DeployedApplication deployedApplication) {
        Set<String> conditions = deployedApplication.getEnvironment().getProperty(ENV_RELEASE_CONDITIONS_PROPERTY);
        return ((conditions == null) ? ImmutableSet.<String>of() : ImmutableSet.copyOf(conditions));
	}

    private static String buildErrorMessage(DeployedApplication deployedApplication,
            Set<ViolatedCondition<?>> violatedConditions) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Cannot deploy '").append(deployedApplication.getName())
        .append("' (version ").append(deployedApplication.getVersion().getVersion())
        .append(") to '").append(deployedApplication.getEnvironment().getName())
        .append("' as the following release conditions are not met:");
        for (ViolatedCondition<?> violatedCondition : violatedConditions) {
            errorMessage.append("\n- '").append(violatedCondition.name).append("'");
        }
        return errorMessage.toString();
    }
}
