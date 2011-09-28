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

import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugins.notifications.email.TestBase;
import com.xebialabs.deployit.plugins.notifications.email.deployed.SentEmail;

/**
 * Unit tests for {@link InjectDeployedApplication}
 */
public class InjectDeployedApplicationTest extends TestBase {

    @Test
    public void injectsDeployedApplication() {
        SentEmail newDeployed = newInstance("notify.BasicSentEmail");
        DeltaSpecification deltaSpec = newDeltaSpec(newEnvironment(), newDeployed).build();
        InjectDeployedApplication.inject(deltaSpec);
        assertThat(newDeployed.getApp(), sameInstance(deltaSpec.getDeployedApplication()));
    }
}
