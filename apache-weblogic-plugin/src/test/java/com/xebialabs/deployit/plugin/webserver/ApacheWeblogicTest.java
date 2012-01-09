package com.xebialabs.deployit.plugin.webserver;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.execution.Plan;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.*;
import com.xebialabs.deployit.plugin.wls.container.Server;
import com.xebialabs.deployit.test.deployment.DeployitTester;
import com.xebialabs.deployit.test.support.LoggingDeploymentExecutionContext;
import com.xebialabs.deployit.test.support.TestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.io.Files.readLines;
import static com.xebialabs.deployit.plugin.webserver.TopologyFactory.wls10gUnixServer1;
import static com.xebialabs.deployit.plugin.webserver.TopologyFactory.wls10gUnixServer2;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ApacheWeblogicTest {

	private final Set<Server> servers;
	private final Container apacheWebServer;
	private final Environment environment;
	private final String generatedServers;


	public ApacheWeblogicTest(Container apacheWebServer, Set<Server> servers) {
		this.apacheWebServer = apacheWebServer;
		this.servers = servers;
		this.environment = TestUtils.createEnvironment(apacheWebServer);
		generatedServers = Joiner.on(',').join(Collections2.transform(servers, new Function<Server, String>() {
			public String apply(Server input) {
				return input.getHost().getProperty("address") + ":" + input.getPort();

			}
		}));
	}

	@Parameterized.Parameters
	public static List<Object[]> getTargetDomains() {
		List<Object[]> targets = newArrayList();
		targets.add(new Object[]{TopologyFactory.apacheServer, singleton(wls10gUnixServer1)});
		targets.add(new Object[]{TopologyFactory.apacheServer, newHashSet(wls10gUnixServer1, wls10gUnixServer2)});
		return targets;
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
		context = new LoggingDeploymentExecutionContext(ApacheWeblogicTest.class);
	}

	@AfterClass
	public static void destroyContext() {
		if (context != null) {
			context.destroy();
		}
	}


	@Test
	public void testTest() throws Exception {
		Deployable apacheWlsSettingSpec = newInstance("www.ApacheWeblogicSettingSpec");
		apacheWlsSettingSpec.setId("App/1.0/wlsApache");
		final Set<String> matchExpressions = Sets.newHashSet("*.jsp");
		apacheWlsSettingSpec.setProperty("matchExpressions", matchExpressions);

		final File docroot = folder.newFolder("docroot");
		final File configurationFragmentDirectory = folder.newFolder("conf");
		apacheWebServer.setProperty("configurationFragmentDirectory", configurationFragmentDirectory.getAbsolutePath());

		final Deployed deployed = tester.generateDeployed(apacheWlsSettingSpec, apacheWebServer, Type.valueOf("www.ApacheWeblogicSetting"));
		deployed.setProperty("port", "80");
		deployed.setProperty("host", "*");
		deployed.setProperty("targets", this.servers);
		deployed.setProperty("documentRoot", docroot.getAbsolutePath());

		final DeploymentPackage deploymentPackageOne = TestUtils.createDeploymentPackage("1.0", apacheWlsSettingSpec);
		final DeployedApplication deployedApplication = TestUtils.createDeployedApplication(deploymentPackageOne, environment);


		DeltaSpecification spec = new DeltaSpecificationBuilder().initial(deployedApplication.getVersion(), deployedApplication.getEnvironment()).create(deployed)
				.build();
		Plan resolvedPlan = tester.resolvePlan(spec);
		List<DeploymentStep> resolvedSteps = resolvedPlan.getSteps();
		assertThat(resolvedSteps.size(), is(3));
		Step.Result result = tester.executePlan(resolvedPlan, context);
		assertThat(result, is(Step.Result.Success));

		final List<String> generatedFile = readLines(new File(configurationFragmentDirectory, deployed.getName() + ".conf"), Charsets.UTF_8);
		assertThat(filter(generatedFile, new Predicate<String>() {
			public boolean apply(String input) {
				return input.contains(generatedServers);
			}
		}).size(), is(1));
		assertThat(filter(generatedFile, new Predicate<String>() {
			public boolean apply(String input) {
				return input.contains("MatchExpression");
			}
		}).size(), is(matchExpressions.size()));


	}


}
