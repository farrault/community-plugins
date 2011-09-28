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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.filter;
import static com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry.getDescriptor;
import static com.xebialabs.deployit.plugin.api.reflect.Types.isSubtypeOf;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.planning.PostPlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugins.notifications.email.ci.MailServer;
import com.xebialabs.deployit.plugins.notifications.email.deployed.SentTemplateEmail;

public class GeneratePreAndPostEmails {
    private static final String ENV_REQUIRES_PRE_EMAIL = "sendDeploymentStartNotification";
    private static final String ENV_REQUIRES_POST_EMAIL = "sendDeploymentEndNotification";
    private static final Type PRE_EMAIL_TYPE = Type.valueOf("notify.DeploymentStartNotification"); 
    private static final Type POST_EMAIL_TYPE = Type.valueOf("notify.DeploymentEndNotification");
    private static final Type MAIL_SERVER_TYPE = Type.valueOf(MailServer.class);
    private static final List<DeploymentStep> NO_STEPS = ImmutableList.of();

    @PrePlanProcessor
    public static List<DeploymentStep> generatePreEmails(DeltaSpecification spec) {
        return generateEmails(spec.getDeployedApplication(), 
                ENV_REQUIRES_PRE_EMAIL, PRE_EMAIL_TYPE);
    }

    @PostPlanProcessor
    public static List<DeploymentStep> generatePostEmails(DeltaSpecification spec) {
        return generateEmails(spec.getDeployedApplication(), 
                ENV_REQUIRES_POST_EMAIL, POST_EMAIL_TYPE);
    }
    
    protected static List<DeploymentStep> generateEmails(DeployedApplication deployedApplication, 
            String triggerProperty, Type sentEmailType) {
        // property may also be null
        if (!TRUE.equals(deployedApplication.getEnvironment()
                .getSyntheticProperty(triggerProperty))) {
            return NO_STEPS;
        }

        StepCollector steps = new StepCollector();
        getDelegate(sentEmailType, deployedApplication).executeCreate(steps);
        return steps.steps;
    }
    
    protected static SentTemplateEmail getDelegate(Type sentEmailType,
            DeployedApplication deployedApplication) {
        SentTemplateEmail delegate = getDescriptor(sentEmailType).newInstance();
        delegate.setContainer(findMailServer(deployedApplication.getEnvironment()));
        delegate.setDeployedApplication(deployedApplication);
        return delegate;
    }

    private static MailServer findMailServer(Environment environment) {
        Set<Container> mailServers = filter(environment.getMembers(),
                new Predicate<Container>() {
                    @Override
                    public boolean apply(Container input) {
                        return isSubtypeOf(MAIL_SERVER_TYPE, input.getType());
                    }
                });
        checkArgument(mailServers.size() == 1, "Cannot send pre- or post-deployment notification emails unless there is exactly 1 'notify.MailServer' in the target environment");
        return (MailServer) mailServers.iterator().next();
    }

    private static class StepCollector implements DeploymentPlanningContext {
        private final List<DeploymentStep> steps = newLinkedList();
        
        @Override
        public void addStep(DeploymentStep step) {
            steps.add(step);
        }

        @Override
        public void addSteps(DeploymentStep... steps) {
            addSteps(asList(steps));
        }

        @Override
        public void addSteps(Collection<DeploymentStep> steps) {
            this.steps.addAll(steps);
        }

        @Override
        public Object getAttribute(String name) {
            throw new UnsupportedOperationException("TODO Auto-generated method stub");
        }

        @Override
        public void setAttribute(String name, Object value) {
            throw new UnsupportedOperationException("TODO Auto-generated method stub");
        }
        
    }
}

