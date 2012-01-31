package com.xebialabs.deployit.plugin.rpm;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.execution.Plan;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.*;
import com.xebialabs.deployit.plugin.overthere.HostContainer;
import com.xebialabs.deployit.test.deployment.DeployitTester;
import com.xebialabs.deployit.test.support.LoggingDeploymentExecutionContext;
import com.xebialabs.deployit.test.support.TestUtils;
import com.xebialabs.overthere.OverthereConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RPMItest {

	private final HostContainer target;
	private final Environment environment;
	private final String deployedType;
	private final String deployableType;

	public RPMItest(HostContainer target, String deployedType, String deployableType) {
		this.target = target;
		this.deployedType = deployedType;
		this.deployableType = deployableType;
		this.environment = TestUtils.createEnvironment(target);
	}

	@Parameterized.Parameters
	public static List<Object[]> getTargetDomains() {
		List<Object[]> targets = newArrayList();
		targets.add(new Object[]{TopologyFactory.sshHost, "rpm.DeployedRPM", "rpm.Package"});
		targets.add(new Object[]{TopologyFactory.rpmContainer, "rpm.DeployedRPMOnContainer", "rpm.ContainerPackage"});
		return targets;
	}

	@Test
	public void deployUpgradeUndeploy() throws Exception {

		final File workingFolder = folder.newFolder("itestrpm");

		//Initial
		final Deployable rpm1 = (Deployable) TestUtils.createArtifact("toto", "1.0", "rpm/toto-0.1-1.i386.rpm", deployableType, workingFolder);
		final DeploymentPackage deploymentPackageOne = TestUtils.createDeploymentPackage("1.0", rpm1);
		final DeployedApplication deployedApplication = TestUtils.createDeployedApplication(deploymentPackageOne, environment);
		final Deployed rpm1Deployed = tester.generateDeployed(rpm1, target, Type.valueOf(deployedType));
		DeltaSpecification spec = new DeltaSpecificationBuilder()
				.initial(deployedApplication.getVersion(), deployedApplication.getEnvironment())
				.create(rpm1Deployed)
				.build();
		assertTrue(spec, "a=1\n");


		//Upgrade
		final Deployable rpm2 = (Deployable) TestUtils.createArtifact("toto", "1.0", "rpm/toto-0.2-1.i386.rpm", deployableType, workingFolder);
		final DeploymentPackage deploymentPackageTwo = TestUtils.createDeploymentPackage("1.0", rpm2);
		final Deployed rpm2Deployed = tester.generateDeployed(rpm2, target, Type.valueOf(deployedType));
		spec = new DeltaSpecificationBuilder()
				.upgrade(deploymentPackageTwo, deployedApplication)
				.modify(rpm1Deployed, rpm2Deployed)
				.build();
		assertTrue(spec, "a=2\n");

		//Delete
		spec = new DeltaSpecificationBuilder()
				.undeploy(deployedApplication)
				.destroy(rpm2Deployed)
				.build();
		assertTrue(spec, null);
	}

	private void assertTrue(DeltaSpecification spec, String expectedContent) throws IOException {
		Plan resolvedPlan = tester.resolvePlan(spec);
		List<DeploymentStep> resolvedSteps = resolvedPlan.getSteps();
		assertThat(resolvedSteps.size(), is(1));
		Step.Result result = tester.executePlan(resolvedPlan, context);
		assertThat(result, is(Step.Result.Success));

		if (expectedContent != null) {
			final OverthereConnection connection = target.getHost().getConnection();
			final InputStream inputStream = connection.getFile("/etc/toto.conf").getInputStream();
			try {
				String content = CharStreams.toString(new InputStreamReader(inputStream, "UTF-8"));
				assertThat(content, is(expectedContent));
			} finally {
				Closeables.closeQuietly(inputStream);
				Closeables.closeQuietly(connection);
			}
		}

	}


	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	protected static DeployitTester tester;
	protected static LoggingDeploymentExecutionContext context;

	static {
		PluginBooter.bootWithoutGlobalContext();
	}

	@BeforeClass
	public static void boot() {
		tester = DeployitTester.build();
		context = new LoggingDeploymentExecutionContext(RPMItest.class);
	}

	@AfterClass
	public static void destroyContext() {
		if (context != null) {
			context.destroy();
		}
	}


}
