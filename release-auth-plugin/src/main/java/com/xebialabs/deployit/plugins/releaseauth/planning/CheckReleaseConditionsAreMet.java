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
package com.xebialabs.deployit.plugins.releaseauth.planning;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.TRUE;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.PostPlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;

public class CheckReleaseConditionsAreMet {
    public static final String ENV_RELEASE_CONDITIONS_PROPERTY = "releaseConditions";
    private static final List<DeploymentStep> NO_STEPS = ImmutableList.of();
    
    // allow other plugins to dynamically set/modify release conditions before validation
    @PostPlanProcessor
    public static List<DeploymentStep> validate(DeltaSpecification spec) {
        Set<String> violatedConditions = validateReleaseConditions(spec.getDeployedApplication());
        if (!violatedConditions.isEmpty()) {
            throw new IllegalArgumentException(buildErrorMessage(
                    spec.getDeployedApplication(), violatedConditions));
        }
        return NO_STEPS;
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

}
