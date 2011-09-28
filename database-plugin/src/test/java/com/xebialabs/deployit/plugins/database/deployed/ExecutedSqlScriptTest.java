/*
 * @(#)ExecutedSqlScriptTest.java     2 Sep 2011
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
package com.xebialabs.deployit.plugins.database.deployed;

import static com.google.common.io.ByteStreams.toByteArray;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.qrmedia.commons.reflect.ReflectionUtils;
import com.xebialabs.deployit.plugins.database.TestBase;

/**
 * Unit tests for {@link ExecutedSqlScript}
 */
public class ExecutedSqlScriptTest extends TestBase {

    @Test
    public void usesSpecifiedSchema() {
        ExecutedSqlScript script = newExecutedSqlScript();
        String schema = "agents";
        script.putSyntheticProperty("schema", schema);
        assertThat(script.getSchemaOrDefault(), is(schema));
    }
    
    @Test
    public void fallsBackToClientDefaultSchema() {
        ExecutedSqlScript script = newExecutedSqlScript();
        String defaultSchema = "agents";
        script.getContainer().putSyntheticProperty("defaultSchema", defaultSchema);
        assertThat(script.getSchemaOrDefault(), is(defaultSchema));
    }
    
    @Test
    public void usesSpecifiedUsername() {
        ExecutedSqlScript script = newExecutedSqlScript();
        String username = "jbond";
        script.putSyntheticProperty("username", username);
        assertThat(script.getUsernameOrDefault(), is(username));
    }
    
    @Test
    public void fallsBackToClientDefaultUsername() {
        ExecutedSqlScript script = newExecutedSqlScript();
        String defaultUsername = "jbond";
        script.getContainer().putSyntheticProperty("defaultUsername", defaultUsername);
        assertThat(script.getUsernameOrDefault(), is(defaultUsername));
    }
    
    @Test
    public void usesSpecifiedPassword() {
        ExecutedSqlScript script = newExecutedSqlScript();
        String password = "007";
        script.putSyntheticProperty("password", password);
        assertThat(script.getPasswordOrDefault(), is(password));
    }
    
    @Test
    public void fallsBackToClientDefaultPassword() {
        ExecutedSqlScript script = newExecutedSqlScript();
        String defaultPassword = "007";
        script.getContainer().putSyntheticProperty("defaultPassword", defaultPassword);
        assertThat(script.getPasswordOrDefault(), is(defaultPassword));
    }
    
    @Test
    public void usesSpecifiedWorkDirectory() throws IllegalAccessException {
        ExecutedSqlScript script = newExecutedSqlScript();
        String workingDirectory = "/tmp/agents";
        script.getContainer().putSyntheticProperty("workingDirectory", workingDirectory);
        StubPlanningContext capturingContext = new StubPlanningContext();
        script.addStep(capturingContext, script.getCreateOrder(), script.getCreateScript(),
                script.getCreateVerb());
        assertThat(ReflectionUtils.<String>getValue(capturingContext.steps.get(0), "specifiedWorkingDir"), 
                is(workingDirectory));
    }
    
    @Test
    public void onlyRunsNewScriptOnModify() throws IllegalAccessException {
        ExecutedSqlScript script = newExecutedSqlScript();
        StubPlanningContext capturingContext = new StubPlanningContext();
        script.updateSqlScript(capturingContext);
        assertThat(capturingContext.steps.size(), is(1));
        ExecutedSqlScript deployed = (ExecutedSqlScript) ReflectionUtils.<Map<String, Object>>
            getValue(capturingContext.steps.get(0), "vars").get("deployed");
        // the deployable determines the SQL script that will be run
        assertThat(deployed.getDeployable(), is(script.getDeployable()));
    }

    @Test
    public void doesNothingOnDestroyWithoutRollbackScript() {
        ExecutedSqlScript script = newExecutedSqlScript();
        StubPlanningContext capturingContext = new StubPlanningContext();
        script.runRollbackSqlScript(capturingContext);
        assertThat(capturingContext.steps.size(), is(0));
    }

    @Test
    public void runsSpecifiedRollbackScriptOnDestroy() throws IllegalAccessException, IOException {
        ExecutedSqlScript script = newExecutedSqlScript();
        script.getDeployable().putSyntheticProperty("rollbackScript", 
                newRollbackScript("select * from AGENTS"));
        StubPlanningContext capturingContext = new StubPlanningContext();
        script.runRollbackSqlScript(capturingContext);
        assertThat(capturingContext.steps.size(), is(1));
        ExecutedSqlScript deployed = (ExecutedSqlScript) ReflectionUtils.<Map<String, Object>>
            getValue(capturingContext.steps.get(0), "vars").get("deployed");
        // the deployable determines the SQL script that will be run
        assertThat(deployed.getDeployable(), 
                is(script.getDeployable().getSyntheticProperty("rollbackScript")));
    }
    
    @Test
    public void replacesPlaceholdersInRollbackScript() throws IllegalAccessException, IOException {
        ExecutedSqlScript script = newExecutedSqlScript();
        script.getDeployable().putSyntheticProperty("rollbackScript", 
                newRollbackScript("select * from AGENTS where name='{{AGENT}}'"));
        script.getPlaceholders().put("AGENT", "James Bond");
        StubPlanningContext capturingContext = new StubPlanningContext();
        // replacement happens when the delegate is created
        script.runRollbackSqlScript(capturingContext);
        ExecutedSqlScript deployed = (ExecutedSqlScript) ReflectionUtils.<Map<String, Object>>
            getValue(capturingContext.steps.get(0), "vars").get("deployed");
        assertThat(new String(toByteArray(deployed.getFile().getInputStream()), Charsets.UTF_8), 
                is("select * from AGENTS where name='James Bond'"));
    }
}