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
import static com.xebialabs.deployit.plugin.api.reflect.Types.isSubtypeOf;
import static com.xebialabs.deployit.plugins.releaseauth.planning.CheckReleaseConditionsAreMet.ENV_RELEASE_CONDITIONS_PROPERTY;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugins.changemgmt.deployed.ChangeTicket;

public class SetChangeTicketReleaseCondition {
    private static final Type CHANGE_MANAGER_TYPE = Type.valueOf("chg.ChangeManager");
    private static final String CHANGE_TICKET_CONDITION_NAME_PROPERTY = "changeTicketReleaseConditionName";
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
            .getSyntheticProperty(ENV_RELEASE_CONDITIONS_PROPERTY);
        if ((releaseConditions == null) || releaseConditions.isEmpty()) {
            LOGGER.debug("No release conditions defined for target environment '{}'",
                    deployedApplication.getEnvironment());
            return;
        }
        
        Version deploymentPackage = deployedApplication.getVersion();
        String changeTicketCondition = getChangeTicketCondition();
        LOGGER.debug("Calculating value of auto-generated change ticket release condition '{}'",
                changeTicketCondition);
        checkState(!deploymentPackage.hasSyntheticProperty(changeTicketCondition),
                "Auto-generated release condition name '%s' conflicts with existing property of the same name on udm.DeploymentPackage. Change the value of property '%s' of %s.",
                changeTicketCondition, CHANGE_TICKET_CONDITION_NAME_PROPERTY, CHANGE_MANAGER_TYPE);

        // looking for a creation or modification of a Change Ticket
        Boolean hasChangeTicket = Boolean.valueOf(
                Iterables.any(spec.getDeltas(), new Predicate<Delta>() {
                    @Override
                    public boolean apply(Delta input) {
                        // operation check first to avoid NPEs
                        return ((input.getOperation().equals(Operation.CREATE)
                                 || input.getOperation().equals(Operation.MODIFY))
                                && isSubtypeOf(Type.valueOf(ChangeTicket.class), 
                                        input.getDeployed().getType()));
                    }
                }));
        deploymentPackage.putSyntheticProperty(changeTicketCondition, hasChangeTicket);
    }

    // not a constant because the class may (?) be loaded before the registry is initialized
    private static String getChangeTicketCondition() {
        return DescriptorRegistry.getDescriptor(CHANGE_MANAGER_TYPE).newInstance()
               .getSyntheticProperty(CHANGE_TICKET_CONDITION_NAME_PROPERTY);
    }
}
