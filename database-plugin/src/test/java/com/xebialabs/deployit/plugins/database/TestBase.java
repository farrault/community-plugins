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
package com.xebialabs.deployit.plugins.database;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.ci.File;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugins.database.deployed.ExecutedSqlScript;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.local.LocalFile;

public abstract class TestBase {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    
    @BeforeClass
    public static void boot() {
        PluginBooter.bootWithoutGlobalContext();
    }
    
    protected static ExecutedSqlScript newExecutedSqlScript() {
        ExecutedSqlScript script = newInstance("sql.ExecutedSqlScript2");
        Container sqlClient = (Container) newInstance("sql.SqlClient");
        sqlClient.setHost(newSshHost());
        script.setContainer(sqlClient);
        script.setDeployable((File) newInstance("sql.SqlScript"));
        return script;
    }
    
    protected File newRollbackScript(String contents) throws IOException {
        File rollbackScript = newInstance("sql.SqlScript");
        rollbackScript.setFile(newLocalFile(contents));
        return rollbackScript;
    }
    
    protected OverthereFile newLocalFile(String contents) throws IOException {
        java.io.File tempFile = temp.newFile(System.currentTimeMillis() + ".tmp.txt");
        Files.write(contents.getBytes(Charsets.UTF_8), tempFile);
        return LocalFile.valueOf(tempFile);
    }
    
    protected static Host newSshHost() {
        Host host = newInstance("overthere.SshHost");
        host.setId("Infrastructure/overthere");
        host.setOs(OperatingSystemFamily.UNIX);
        host.putSyntheticProperty(CONNECTION_TYPE, SFTP);
        host.putSyntheticProperty(ADDRESS, "overthere");
        host.putSyntheticProperty(USERNAME, "overthere");
        host.putSyntheticProperty(PASSWORD, "overhere");
        return host;
    }
    
    protected static class StubPlanningContext implements DeploymentPlanningContext {
        public final List<DeploymentStep> steps = newArrayList();
        
        public StubPlanningContext() {}
        
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
    }
}
