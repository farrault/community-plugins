/*
 * @(#)TestBase.java     3 Sep 2011
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
package com.xebialabs.deployit.plugins.tests.deployed;


import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugins.tests.TestBase;

/**
 * Unit tests for the {@link HttpRequestTestExecution}
 */
public class HttpRequestTestExecutionTest extends TestBase {

    @BeforeClass
    public static void boot() {
        PluginBooter.bootWithoutGlobalContext();
    }

    @Test
    public void doesNothingOnDestroy() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        HttpRequestTestExecution test = newInstance("tests.HttpRequestTestExecution2");
        test.executeDestroy(capturingContext);
        assertThat(capturingContext.steps.size(), is(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void requiresUnixOsFamily() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        HttpRequestTestExecution test = newInstance("tests.HttpRequestTestExecution2");
        test.setContainer(newTestStation(newCifsHost()));
        test.executeCreate(capturingContext);
    }
    
    private static Container newTestStation(Host host) {
        Container testStation = newInstance("tests.TestStation");
        testStation.setHost(host);
        return testStation;
    }
}
