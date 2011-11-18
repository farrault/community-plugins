/*
 * @(#)CheckReleaseConditionsAreMetTest.java     26 Sep 2011
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
import static com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder.newSpecification;
import static com.xebialabs.deployit.test.support.TestUtils.createDeploymentPackage;
import static com.xebialabs.deployit.test.support.TestUtils.createEnvironment;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import org.junit.BeforeClass;
import org.junit.Test;

import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.test.yak.ci.YakServer;

/**
 * Unit tests for {@link CheckReleaseConditionsAreMet}
 */
public class CheckReleaseConditionsAreMetTest {
    
    @BeforeClass
    public static void boot() {
        PluginBooter.bootWithoutGlobalContext();
    }
    
    @Test
    public void ignoresEnvironmentsWithoutReleaseConditions() {
        CheckReleaseConditionsAreMet.validate(newDeltaSpec(newEnvironment()).build());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void failsIfAReleaseConditionsIsNull() {
        Environment env = newEnvironment();
        env.setProperty("releaseConditions", newHashSet("hasReleaseNotes"));
        CheckReleaseConditionsAreMet.validate(newDeltaSpec(env).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfAReleaseConditionsIsFalse() {
        Environment env = newEnvironment();
        env.setProperty("releaseConditions", newHashSet("hasReleaseNotes"));
        DeltaSpecification deltaSpec = newDeltaSpec(env).build();
        deltaSpec.getDeployedApplication().getVersion().setProperty("hasReleaseNotes", FALSE);
        CheckReleaseConditionsAreMet.validate(deltaSpec);
    }
    
    @Test
    public void succeedsIfReleaseConditionsAreMet() {
        Environment env = newEnvironment();
        env.setProperty("releaseConditions", newHashSet("hasReleaseNotes"));
        DeltaSpecification deltaSpec = newDeltaSpec(env).build();
        deltaSpec.getDeployedApplication().getVersion().setProperty("hasReleaseNotes", TRUE);
        CheckReleaseConditionsAreMet.validate(deltaSpec);
    }
    
    private static Environment newEnvironment() {
        return createEnvironment((YakServer) newInstance(YakServer.class));
    }
    
    private static DeltaSpecificationBuilder newDeltaSpec(Environment env,
            Deployed<?, ?>... newDeployeds) {
        DeltaSpecificationBuilder deltaSpec = 
            newSpecification().initial(createDeploymentPackage(), env);
        for (Deployed<?, ?> newDeployed : newDeployeds) {
            deltaSpec.create(newDeployed);
        }
        return deltaSpec;
    }
}
