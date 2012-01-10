package com.xebialabs.deployit.plugin.webserver;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.execution.Plan;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.*;
import com.xebialabs.deployit.plugin.generic.deployed.ProcessedTemplate;
import com.xebialabs.deployit.test.deployment.DeployitTester;
import com.xebialabs.deployit.test.support.LoggingDeploymentExecutionContext;
import com.xebialabs.deployit.test.support.TestUtils;
import org.hamcrest.Matchers;
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

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.io.Files.readLines;
import static com.xebialabs.deployit.plugin.webserver.TopologyFactory.jbossContainer1;
import static com.xebialabs.deployit.plugin.webserver.TopologyFactory.jbossContainer2;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ApacheModJKTest {

	private final Set<Container> servers;
	private final Container apacheWebServer;
	private final Environment environment;

	public ApacheModJKTest(Container apacheWebServer, Set<Container> servers) {
		this.apacheWebServer = apacheWebServer;
		this.servers = servers;
		this.environment = TestUtils.createEnvironment(apacheWebServer);

	}

	@Parameterized.Parameters
	public static List<Object[]> getTargetDomains() {
		List<Object[]> targets = newArrayList();
		targets.add(new Object[]{TopologyFactory.apacheServer, singleton(jbossContainer1)});
		targets.add(new Object[]{TopologyFactory.apacheServer, newHashSet(jbossContainer1, jbossContainer2)});
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
		context = new LoggingDeploymentExecutionContext(ApacheModJKTest.class);
	}

	@AfterClass
	public static void destroyContext() {
		if (context != null) {
			context.destroy();
		}
	}


	@Test
	public void testTest() throws Exception {

		final File docroot = folder.newFolder("docroot");
		final File configurationFragmentDirectory = folder.newFolder("conf");
		final File logDir = folder.newFolder("logDir");
		apacheWebServer.setProperty("configurationFragmentDirectory", configurationFragmentDirectory.getAbsolutePath());
		apacheWebServer.setProperty("logDirectory", logDir.getAbsolutePath());


		//workers
		Deployable workerSpec = newInstance("www.ApacheModJKWorkerSpec");
		workerSpec.setId("App/1.0/PetClinic-worker-jk");

		//general configuration
		Deployable modjkSpec = newInstance("www.ApacheModJKSpec");
		modjkSpec.setId("App/1.0/PetClinic-jk");

		//mount virtual hosts
		Deployable modjkVhSpec = newInstance("www.ApacheVirtualHostModJKSpec");
		modjkVhSpec.setId("App/1.0/PetClinic-vh-jk");
		final Set<String> mountedContexts = Sets.newHashSet("/petclinic/*", "/admin/*");
		final Set<String> unmountedContexts = Sets.newHashSet("/img/*");
		modjkVhSpec.setProperty("mountedContexts", mountedContexts);
		modjkVhSpec.setProperty("unmountedContexts", unmountedContexts);

		final DeploymentPackage deploymentPackageOne = TestUtils.createDeploymentPackage("1.0", workerSpec, modjkSpec, modjkVhSpec);


		final Deployed workerSettting = tester.generateDeployed(workerSpec, apacheWebServer, Type.valueOf("www.ApacheModJKWorkerSetting"));
		workerSettting.setProperty("targets", this.servers);

		final Deployed modjkSettting = tester.generateDeployed(modjkSpec, apacheWebServer, Type.valueOf("www.ApacheModJKSetting"));

		final ProcessedTemplate modjkVHSettting = (ProcessedTemplate) tester.generateDeployed(modjkVhSpec, apacheWebServer, Type.valueOf("www.ApacheVirtualHostModJKSetting"));
		modjkVHSettting.setProperty("port", "80");
		modjkVHSettting.setProperty("host", "myremote.com");
		modjkVHSettting.setProperty("documentRoot", docroot.getAbsolutePath());


		final DeployedApplication deployedApplication = TestUtils.createDeployedApplication(deploymentPackageOne, environment);


		DeltaSpecification spec = new DeltaSpecificationBuilder()
				.initial(deployedApplication.getVersion(), deployedApplication.getEnvironment())
				.create(workerSettting)
				.create(modjkSettting)
				.create(modjkVHSettting)
				.build();
		Plan resolvedPlan = tester.resolvePlan(spec);
		List<DeploymentStep> resolvedSteps = resolvedPlan.getSteps();
		assertThat(resolvedSteps.size(), is(5));
		Step.Result result = tester.executePlan(resolvedPlan, context);
		assertThat(result, is(Step.Result.Success));

		//moddjk.conf
		final List<String> modjkDotConf = readLines(new File(configurationFragmentDirectory, modjkSettting.<String>getProperty("targetFile")), Charsets.UTF_8);
		final File workerPropertyFile = new File(configurationFragmentDirectory, workerSettting.<String>getProperty("targetFile"));
		assertThat(Iterables.find(modjkDotConf, new Predicate<String>() {
			public boolean apply(String input) {
				return input.startsWith("JkWorkersFile");
			}
		}), Matchers.endsWith(workerPropertyFile.getAbsolutePath()));
		assertThat(Iterables.find(modjkDotConf, new Predicate<String>() {
			public boolean apply(String input) {
				return input.startsWith("JkLogFile");
			}
		}), Matchers.endsWith(new File(logDir, modjkSettting.<String>getProperty("logFile")).getAbsolutePath()));


		//VHost
		final List<String> modjkVHDotConf = readLines(new File(configurationFragmentDirectory, modjkVHSettting.getTargetFile()), Charsets.UTF_8);
		assertThat(filter(modjkVHDotConf, new Predicate<String>() {
			public boolean apply(String input) {
				return input.contains("JkMount");
			}
		}).size(), is(mountedContexts.size()));
		assertThat(filter(modjkVHDotConf, new Predicate<String>() {
			public boolean apply(String input) {
				return input.contains("JkUnMount");
			}
		}).size(), is(unmountedContexts.size()));


		//Workers
		final String balance_workers = on(',').join(transform(servers, new Function<Container, String>() {
			public String apply(Container input) {
				return "worker-" + input.getName();
			}
		}));
		final List<String> workerDotProp = readLines(workerPropertyFile, Charsets.UTF_8);
		assertThat(filter(workerDotProp, new Predicate<String>() {
			public boolean apply(String input) {
				return input.contains(on("=").join("worker.LB.balance_workers",balance_workers));
			}
		}).size(), is(1));

	}


}
