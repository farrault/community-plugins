/*
 * @(#)GeneratePreAndPostEmailsTest.java     24 Sep 2011
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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugins.notifications.email.TestBase;
import com.xebialabs.deployit.plugins.notifications.email.step.TemplateEmailSendStep;

/**
 * Unit tests for {@link GeneratePreAndPostEmails}
 */
public class GeneratePreAndPostEmailsTest extends TestBase {

    @Test
    public void generatesPreEmail() {
        Environment env = newEnvironment();
        env.setProperty("sendDeploymentStartNotification", true);
        List<DeploymentStep> steps = GeneratePreAndPostEmails.generatePreEmails(
                newDeltaSpec(env).build());
        assertThat(steps.size(), is(1));
        assertThat(steps.get(0), instanceOf(TemplateEmailSendStep.class));
    }
    
    @Test
    public void generatesPostEmail() {
        Environment env = newEnvironment();
        env.setProperty("sendDeploymentEndNotification", true);
        List<DeploymentStep> steps = GeneratePreAndPostEmails.generatePostEmails(
                newDeltaSpec(env).build());
        assertThat(steps.size(), is(1));
        assertThat(steps.get(0), instanceOf(TemplateEmailSendStep.class));
    }
}
