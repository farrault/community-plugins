/*
 * @(#)SqlScriptOrdererTest.java     2 Sep 2011
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
package com.xebialabs.deployit.plugins.database.planning;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.qrmedia.commons.reflect.ReflectionUtils;
import com.xebialabs.deployit.deployment.planner.DefaultDelta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugins.database.TestBase;
import com.xebialabs.deployit.plugins.database.deployed.ExecutedSqlScript;

/**
 * Unit tests for {@link SqlScriptOrderer}
 */
public class SqlScriptOrdererTest extends TestBase {
    private final SqlScriptOrderer orderer = new SqlScriptOrderer();
    
    @Test
    public void callsCreateForNewScripts() {
        ExecutedSqlScript script = newMockExecutedSqlScript();
        StubPlanningContext stubContext = new StubPlanningContext();
        script.runSqlScript(stubContext);
        expectLastCall();
        replay(script);
        orderer.orderSqlScripts(newDeltas(create(script)), stubContext);
        verify(script);
    }

    @Test
    public void callsUpdateForModifiedScripts() {
        ExecutedSqlScript script = newMockExecutedSqlScript();
        StubPlanningContext stubContext = new StubPlanningContext();
        script.updateSqlScript(stubContext);
        expectLastCall();
        replay(script);
        orderer.orderSqlScripts(newDeltas(modify(newExecutedSqlScript(), script)), stubContext);
        verify(script);
    }
    
    @Test
    public void callsRollbackForDeletedScripts() {
        ExecutedSqlScript script = newMockExecutedSqlScript();
        StubPlanningContext stubContext = new StubPlanningContext();
        script.runRollbackSqlScript(stubContext);
        expectLastCall();
        replay(script);
        orderer.orderSqlScripts(newDeltas(destroy(script)), stubContext);
        verify(script);
    }
    
    @Test
    public void ordersFirstByContainer() throws IllegalAccessException {
        ExecutedSqlScript script1 = newExecutedSqlScript();
        script1.getDeployable().putSyntheticProperty("order", 2);
        // ensure known container ID order
        script1.getContainer().setId("0");
        ExecutedSqlScript script2 = newExecutedSqlScript();
        script2.getDeployable().putSyntheticProperty("order", 1);
        script2.getContainer().setId("1");
        StubPlanningContext capturingContext = new StubPlanningContext();
        orderer.orderSqlScripts(newDeltas(create(script1), create(script2)), capturingContext);
        assertThat(capturingContext.steps.size(), is(2));
        assertThat(ReflectionUtils.<Container>getValue(
                capturingContext.steps.get(0), "container").getId(), 
                is(script1.getContainer().getId()));
        assertThat(ReflectionUtils.<Container>getValue(
                capturingContext.steps.get(1), "container").getId(), 
                is(script2.getContainer().getId()));
    }
    
    @Test
    public void ordersNewScriptsAscendingInContainer() throws IllegalAccessException {
        ExecutedSqlScript script1 = newExecutedSqlScript();
        script1.getDeployable().putSyntheticProperty("order", 2);
        ExecutedSqlScript script2 = newExecutedSqlScript();
        script2.getDeployable().putSyntheticProperty("order", 1);
        script2.setContainer(script1.getContainer());
        StubPlanningContext capturingContext = new StubPlanningContext();
        orderer.orderSqlScripts(newDeltas(create(script1), create(script2)), capturingContext);
        assertThat(capturingContext.steps.size(), is(2));
        ExecutedSqlScript deployed1 = (ExecutedSqlScript) ReflectionUtils.<Map<String, Object>>
            getValue(capturingContext.steps.get(0), "vars").get("deployed");
        assertThat(deployed1, is(script2));
        ExecutedSqlScript deployed2 = (ExecutedSqlScript) ReflectionUtils.<Map<String, Object>>
            getValue(capturingContext.steps.get(1), "vars").get("deployed");
        assertThat(deployed2, is(script1));
    }
    
    @Test
    public void ordersRollbackScriptsDescendingInContainer() throws IllegalAccessException, IOException {
        ExecutedSqlScript script1 = newExecutedSqlScript();
        script1.getDeployable().putSyntheticProperty("order", 2);
        script1.getDeployable().putSyntheticProperty("rollbackScript", 
                newRollbackScript("select * from AGENTS"));
        ExecutedSqlScript script2 = newExecutedSqlScript();
        script2.getDeployable().putSyntheticProperty("order", 1);
        script2.getDeployable().putSyntheticProperty("rollbackScript", 
                newRollbackScript("select * from AGENTS"));
        script2.setContainer(script1.getContainer());
        StubPlanningContext capturingContext = new StubPlanningContext();
        orderer.orderSqlScripts(newDeltas(destroy(script1), destroy(script2)), capturingContext);
        assertThat(capturingContext.steps.size(), is(2));
        ExecutedSqlScript deployed1 = (ExecutedSqlScript) ReflectionUtils.<Map<String, Object>>
            getValue(capturingContext.steps.get(0), "vars").get("deployed");
        // can't compare Deployeds directly because they use delegates
        assertThat(deployed1.getDeployable(), 
                is(script1.getDeployable().getSyntheticProperty("rollbackScript")));
        ExecutedSqlScript deployed2 = (ExecutedSqlScript) ReflectionUtils.<Map<String, Object>>
            getValue(capturingContext.steps.get(1), "vars").get("deployed");
        assertThat(deployed2.getDeployable(), 
                is(script2.getDeployable().getSyntheticProperty("rollbackScript")));
    }
    
    private static ExecutedSqlScript newMockExecutedSqlScript() {
        ExecutedSqlScript script = createMock(ExecutedSqlScript.class);
        expect(script.getType()).andStubReturn(Type.valueOf(ExecutedSqlScript.class));
        return script;
    }

    private static Deltas newDeltas(Delta... deltas) {
        return new Deltas(asList(deltas));
    }
    
    private static Delta create(Deployed<?, ?> newDeployed) {
        return new DefaultDelta(Operation.CREATE, null, newDeployed);
    }
    
    private static Delta modify(Deployed<?, ?> previousDeployed, Deployed<?, ?> newDeployed) {
        return new DefaultDelta(Operation.MODIFY, previousDeployed, newDeployed);
    }
    
    private static Delta destroy(Deployed<?, ?> previousDeployed) {
        return new DefaultDelta(Operation.DESTROY, previousDeployed, null);
    }
}
