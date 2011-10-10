package com.xebialabs.deployit.plugins.tests;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.deployit.test.support.TestUtils.createDeploymentPackage;
import static com.xebialabs.deployit.test.support.TestUtils.createEnvironment;
import static com.xebialabs.deployit.test.support.TestUtils.id;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;

import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.udm.Application;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.DeploymentPackage;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.overthere.OperatingSystemFamily;

public abstract class TestBase {

	public Environment environment;
	
    @BeforeClass
    public static void boot() {
        PluginBooter.bootWithoutGlobalContext();
    }

	public DeployedApplication newDeployedApplication(String name, String version, Container container, Deployable... deployables) {
		DeploymentPackage pkg = createDeploymentPackage(version, deployables);
		Environment env = createEnvironment(container);
		DeployedApplication deployedApp = newInstance("udm.DeployedApplication");
		deployedApp.setVersion(pkg);
		deployedApp.setEnvironment(env);
		deployedApp.setId(id(env.getId(), name));
		return deployedApp;
	}

    public static DeploymentPackage createDeploymentPackage(String version, Deployable... deployables) {
        Application app = newInstance(Application.class);
        app.setId(id("Applications", "Test"));
        DeploymentPackage pkg = newInstance(DeploymentPackage.class);
        pkg.setId(id(pkg.getId(), version));
        pkg.setApplication(app);

        for (Deployable deployable : deployables) {
            deployable.setId(id(app.getId(), deployable.getId()));
            pkg.addDeployable(deployable);
        }

        return pkg;
    }

    public static Environment createEnvironment(Container... containers) {
        Environment env = newInstance(Environment.class);
        env.setId(id("Environments", "JUnit"));
        Set<Container> containerSet = newHashSet();
        containerSet.addAll(Arrays.asList(containers));
        env.setMembers(containerSet);
        return env;
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