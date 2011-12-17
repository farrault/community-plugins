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
package com.xebialabs.deployit.plugins.notifications.email.planning;

import static com.google.common.collect.Iterables.filter;
import static com.xebialabs.deployit.plugin.api.util.Predicates.deltaOf;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugins.notifications.email.deployed.SentEmail;

public class InjectDeployedApplication {
    private static final Predicate<Delta> IS_SENT_EMAIL = 
        deltaOf(Type.valueOf(SentEmail.class));
    private static final List<DeploymentStep> NO_STEPS = ImmutableList.of();
    
    @PrePlanProcessor
    public static List<DeploymentStep> inject(DeltaSpecification spec) {
        injectDeployedApplication(spec);
        return NO_STEPS;
    }
    
    protected static void injectDeployedApplication(DeltaSpecification spec) {
        DeployedApplication deployedApplication = spec.getDeployedApplication();
        for (Delta delta : filter(spec.getDeltas(), IS_SENT_EMAIL)) {
            Deployed<?, ?> deployed = delta.getDeployed();
            if (deployed != null) {
                ((SentEmail) deployed).setDeployedApplication(deployedApplication); 
            }
        }
    }
}
