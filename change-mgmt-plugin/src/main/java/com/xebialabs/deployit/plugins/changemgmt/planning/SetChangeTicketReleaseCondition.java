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
package com.xebialabs.deployit.plugins.changemgmt.planning;

import static com.google.common.base.Preconditions.checkState;
import static com.xebialabs.deployit.community.releaseauth.planning.CheckReleaseConditionsAreMet.ENV_RELEASE_CONDITIONS_PROPERTY;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.reflect.Types;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.DeploymentPackage;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugins.changemgmt.deployed.ChangeTicket;

public class SetChangeTicketReleaseCondition {
    @VisibleForTesting
    static final String CHANGE_TICKET_CONDITION_NAME_PROPERTY = "changeTicketReleaseConditionName";
    private static final Type DEPLOYMENT_PACKAGE_TYPE = Type.valueOf(DeploymentPackage.class);
    private static final Type CHANGE_MANAGER_TYPE = Type.valueOf("chg.ChangeManager");
    private static final List<DeploymentStep> NO_STEPS = ImmutableList.of();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SetChangeTicketReleaseCondition.class);
    
    @PrePlanProcessor
    public static List<DeploymentStep> setReleaseCondition(DeltaSpecification spec) {
        setChangeTicketCondition(spec);
        return NO_STEPS;
    }

    protected static void setChangeTicketCondition(DeltaSpecification spec) {
        DeployedApplication deployedApplication = spec.getDeployedApplication();
        Set<String> releaseConditions = deployedApplication.getEnvironment()
            .getProperty(ENV_RELEASE_CONDITIONS_PROPERTY);
        if ((releaseConditions == null) || releaseConditions.isEmpty()) {
            LOGGER.debug("No release conditions defined for target environment '{}'",
                    deployedApplication.getEnvironment());
            return;
        }
        
        Version deploymentPackage = deployedApplication.getVersion();
        String changeTicketCondition = getChangeTicketCondition();
        checkState(deploymentPackage.hasProperty(changeTicketCondition),
                "No release condition '%s' defined for %s. Define a boolean, hidden property of this name on %s or change the value of property '%s' of %s.",
                changeTicketCondition, DEPLOYMENT_PACKAGE_TYPE, DEPLOYMENT_PACKAGE_TYPE, CHANGE_TICKET_CONDITION_NAME_PROPERTY, CHANGE_MANAGER_TYPE);
        // can't use a constant in case the descriptor registry is refreshed
        checkState(DescriptorRegistry.getDescriptor(DEPLOYMENT_PACKAGE_TYPE)
        		   .getPropertyDescriptor(changeTicketCondition).isHidden(),
                "Release condition '%s' is not defined as 'hidden' on '%s'. Hide it or change the value of property '%s' of %s.",
                changeTicketCondition, DEPLOYMENT_PACKAGE_TYPE, CHANGE_TICKET_CONDITION_NAME_PROPERTY, CHANGE_MANAGER_TYPE);
        LOGGER.debug("Calculating value of hidden change ticket release condition '{}'",
        		changeTicketCondition);

        /*
         * Always allow undeployments. Not great, but where would a user put the
         * change ticket number? For initial/upgrade installations, looks for 
         * a creation or modification of a Change Ticket.
         */
        Boolean hasChangeTicket = spec.getOperation().equals(Operation.DESTROY)
                || Boolean.valueOf(Iterables.any(spec.getDeltas(), new Predicate<Delta>() {
                        @Override
                        public boolean apply(Delta input) {
                            // operation check first to avoid NPEs
                            return ((input.getOperation().equals(Operation.CREATE)
                                     || input.getOperation().equals(Operation.MODIFY))
                                    && Types.isSubtypeOf(Type.valueOf(ChangeTicket.class), 
                                            input.getDeployed().getType()));
                        }
                    }));
        deploymentPackage.setProperty(changeTicketCondition, hasChangeTicket);
    }

    // not a constant because the class may (?) be loaded before the registry is initialized
    private static String getChangeTicketCondition() {
        return DescriptorRegistry.getDescriptor(CHANGE_MANAGER_TYPE).newInstance()
               .getProperty(CHANGE_TICKET_CONDITION_NAME_PROPERTY);
    }
}
