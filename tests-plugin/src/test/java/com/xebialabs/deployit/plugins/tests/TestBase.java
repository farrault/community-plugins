package com.xebialabs.deployit.plugins.tests;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;

import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.overthere.OperatingSystemFamily;

public abstract class TestBase {

    @BeforeClass
    public static void boot() {
        PluginBooter.bootWithoutGlobalContext();
    }

    protected static Host newCifsHost() {
        Host host = newInstance("overthere.CifsHost");
        host.setId("Infrastructure/overthere");
        host.setOs(OperatingSystemFamily.WINDOWS);
        host.putSyntheticProperty(CONNECTION_TYPE, TELNET);
        host.putSyntheticProperty(ADDRESS, "overthere");
        host.putSyntheticProperty(USERNAME, "overthere");
        host.putSyntheticProperty(PASSWORD, "overhere");
        return host;
    }
    
    public static class StubPlanningContext implements DeploymentPlanningContext {
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
    }
}
