/*
 * @(#)CheckRequiredChangeRequestsTest.java     26 Aug 2011
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

import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.deployit.community.releaseauth.planning.CheckReleaseConditionsAreMet.ENV_RELEASE_CONDITIONS_PROPERTY;
import static com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder.newSpecification;
import static com.xebialabs.deployit.plugins.changemgmt.planning.SetChangeTicketReleaseCondition.CHANGE_TICKET_CONDITION_NAME_PROPERTY;
import static com.xebialabs.deployit.test.support.TestUtils.createDeployedApplication;
import static com.xebialabs.deployit.test.support.TestUtils.createDeploymentPackage;
import static com.xebialabs.deployit.test.support.TestUtils.createEnvironment;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.test.yak.ci.YakServer;
import com.xebialabs.deployit.plugins.changemgmt.OverrideTestSynthetics;
import com.xebialabs.deployit.plugins.changemgmt.deployed.ChangeTicket;
import com.xebialabs.deployit.test.support.TestUtils;

/**
 * Unit tests for {@link SetChangeTicketReleaseCondition}
 */
public class SetChangeTicketReleaseConditionTest {
    private static String changeTicketReleaseCondition;
    
    @Rule
    public OverrideTestSynthetics syntheticOverride = new OverrideTestSynthetics("src/test/resources");
    
    @Before
    public void boot() {
        PluginBooter.bootWithoutGlobalContext();
        changeTicketReleaseCondition = TestUtils.<Container>newInstance("chg.ChangeManager")
            .getProperty(CHANGE_TICKET_CONDITION_NAME_PROPERTY);
    }
    
    @Test(expected = IllegalStateException.class)
    public void failsIfReleaseConditionIsNotDefined() {
        Environment env = newEnvironment();
        env.setProperty(ENV_RELEASE_CONDITIONS_PROPERTY, newHashSet(changeTicketReleaseCondition));
        SetChangeTicketReleaseCondition.setReleaseCondition(newDeltaSpec(env).build());
    }

    @Test(expected = IllegalStateException.class)
    public void failsIfReleaseConditionIsNotHidden() {
        Environment env = newEnvironment();
        env.setProperty(ENV_RELEASE_CONDITIONS_PROPERTY, newHashSet(changeTicketReleaseCondition));
        SetChangeTicketReleaseCondition.setReleaseCondition(newDeltaSpec(env).build());
    }
    
    @Test
    public void ignoresEnvironmentsWithoutReleaseCondition() {
        SetChangeTicketReleaseCondition.setReleaseCondition(newDeltaSpec(newEnvironment()).build());
    }

    @Test
    public void unsetsConditionIfNoChangeTicketIsCreatedOrModified() {
        Environment env = newEnvironment();
        env.addMember((Container) newInstance("chg.ChangeManager"));
        env.setProperty(ENV_RELEASE_CONDITIONS_PROPERTY, 
                newHashSet(changeTicketReleaseCondition));
        DeltaSpecification deltaSpec = newDeltaSpec(env).build();
        SetChangeTicketReleaseCondition.setReleaseCondition(deltaSpec);
        assertThat(deltaSpec.getDeployedApplication().getVersion()
            .getProperty(changeTicketReleaseCondition), Is.<Object>is(FALSE));
    }    
    
    @Test
    public void setsConditionIfChangeTicketIsCreated() {
        Environment env = newEnvironment();
        env.addMember((Container) newInstance("chg.ChangeManager"));
        env.setProperty(ENV_RELEASE_CONDITIONS_PROPERTY, 
                newHashSet(changeTicketReleaseCondition));
        DeltaSpecification deltaSpec = newDeltaSpec(env, 
                TestUtils.<ChangeTicket>newInstance("chg.ChangeTicket2")).build();
        SetChangeTicketReleaseCondition.setReleaseCondition(deltaSpec);
        assertThat(deltaSpec.getDeployedApplication().getVersion()
            .getProperty(changeTicketReleaseCondition), Is.<Object>is(TRUE));
    }

    @Test
    public void setsConditionIfChangeTicketIsOrModified() {
        Environment env = newEnvironment();
        env.addMember((Container) newInstance("chg.ChangeManager"));
        env.setProperty(ENV_RELEASE_CONDITIONS_PROPERTY, 
                newHashSet(changeTicketReleaseCondition));
        DeltaSpecificationBuilder specBuilder = newDeltaSpec(env);
        specBuilder.modify(TestUtils.<ChangeTicket>newInstance("chg.ChangeTicket2"),
            TestUtils.<ChangeTicket>newInstance("chg.ChangeTicket2"));
        DeltaSpecification deltaSpec = specBuilder.build();
        SetChangeTicketReleaseCondition.setReleaseCondition(deltaSpec);
        assertThat(deltaSpec.getDeployedApplication().getVersion()
            .getProperty(changeTicketReleaseCondition), Is.<Object>is(TRUE));
    }
    
    // not great, but where would a user put the change ticket for undeployment?
    @Test
    public void setsConditionForUndeployment() {
        Environment env = newEnvironment();
        env.addMember((Container) newInstance("chg.ChangeManager"));
        env.setProperty(ENV_RELEASE_CONDITIONS_PROPERTY, 
                newHashSet(changeTicketReleaseCondition));
        DeltaSpecificationBuilder specBuilder = newSpecification().undeploy(
                createDeployedApplication(createDeploymentPackage(), env));
        specBuilder.destroy(TestUtils.<ChangeTicket>newInstance("chg.ChangeTicket2"));
        DeltaSpecification deltaSpec = specBuilder.build();
        SetChangeTicketReleaseCondition.setReleaseCondition(deltaSpec);
        assertThat(deltaSpec.getDeployedApplication().getVersion()
            .getProperty(changeTicketReleaseCondition), Is.<Object>is(TRUE));
    }
    
    private static Environment newEnvironment() {
        return createEnvironment((YakServer) newInstance("yak.YakServer"));
    }
    
    private static DeltaSpecificationBuilder newDeltaSpec(Environment env,
            Deployed<?, ?>... newDeployeds) {
        DeltaSpecificationBuilder deltaSpec = 
            newSpecification().initial(createDeploymentPackage(), env);
        deltaSpec.create((Deployed<?, ?>) newInstance("yak.DeployedYakFile"));
        for (Deployed<?, ?> newDeployed : newDeployeds) {
            deltaSpec.create(newDeployed);
        }
        return deltaSpec;
    }
}