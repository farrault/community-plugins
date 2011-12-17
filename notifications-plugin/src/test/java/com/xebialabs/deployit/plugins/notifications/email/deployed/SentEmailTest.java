/*
 * @(#)SentEmailTest.java     24 Sep 2011
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
package com.xebialabs.deployit.plugins.notifications.email.deployed;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugins.notifications.email.TestBase;
import com.xebialabs.deployit.plugins.notifications.email.step.EmailSendStep;

public class SentEmailTest extends TestBase {

    @Test
    public void supportsNoTos() {
        SentEmail newDeployed = newInstance("notify.BasicSentEmail");
        // just to be sure
        newDeployed.setProperty("to", null);
        assertThat(newDeployed.getToAddresses().size(), is(0));
    }
    
    @Test
    public void supportsNoCcs() {
        SentEmail newDeployed = newInstance("notify.BasicSentEmail");
        // just to be sure
        newDeployed.setProperty("cc", null);
        assertThat(newDeployed.getCcAddresses().size(), is(0));
    }
    
    @Test
    public void supportsNoBccs() {
        SentEmail newDeployed = newInstance("notify.BasicSentEmail");
        // just to be sure
        newDeployed.setProperty("bcc", null);
        assertThat(newDeployed.getBccAddresses().size(), is(0));
    }

    @Test
    public void defaultsToNoAwaitCompletionStep() {
        SentEmail newDeployed = newInstance("notify.BasicSentEmail");
        StubPlanningContext capturingContext = new StubPlanningContext();
        newDeployed.executeCreate(capturingContext);
        List<DeploymentStep> steps = capturingContext.steps;
        assertThat(steps.size(), is(1));
        assertThat(steps.get(0), instanceOf(EmailSendStep.class));
    }

    @Test
    public void addsAwaitCompletionStepIfRequested() {
        SentEmail newDeployed = newInstance("notify.BasicSentEmail");
        newDeployed.setProperty("awaitConfirmation", true);
        StubPlanningContext capturingContext = new StubPlanningContext();
        newDeployed.executeCreate(capturingContext);
        List<DeploymentStep> steps = capturingContext.steps;
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), instanceOf(EmailSendStep.class));
        // also a GenericBaseStep but *not* an EmailSendStep
        assertThat(steps.get(1), not((instanceOf(EmailSendStep.class))));
    }

    private static class StubPlanningContext implements DeploymentPlanningContext {
        public final List<DeploymentStep> steps = newArrayList();

        @Override
        public void addStep(DeploymentStep deploymentstep) {
            addSteps(deploymentstep);
        }

        @Override
        public void addSteps(DeploymentStep... adeploymentstep) {
            addSteps(asList(adeploymentstep));
        }

        @Override
        public void addSteps(Collection<DeploymentStep> arg0) {
            steps.addAll(arg0);
        }

        @Override
        public Object getAttribute(String s) {
            throw new UnsupportedOperationException("TODO Auto-generated method stub");
        }

        @Override
        public void setAttribute(String s, Object obj) {
            throw new UnsupportedOperationException("TODO Auto-generated method stub");
        }

		@Override
		public DeployedApplication getDeployedApplication() {
			throw new UnsupportedOperationException("TODO Auto-generated method stub");
		}
    }
}