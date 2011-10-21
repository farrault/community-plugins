package com.xebialabs.deployit.plugins.database;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.newReaderSupplier;
import static com.xebialabs.deployit.test.support.TestUtils.createDeploymentPackage;
import static com.xebialabs.deployit.test.support.TestUtils.createEnvironment;
import static com.xebialabs.deployit.test.support.TestUtils.id;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.itest.ItestHostFactory.getItestHost;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;
import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.execution.Plan;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.execution.Step;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.DeploymentPackage;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.api.udm.artifact.SourceArtifact;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.ci.Folder;
import com.xebialabs.deployit.plugin.generic.deployed.ExecutedFolder;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.test.deployment.DeployitTester;
import com.xebialabs.deployit.test.support.LoggingDeploymentExecutionContext;
import com.xebialabs.itest.ItestHost;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.local.LocalFile;

@SuppressWarnings({"unchecked", "rawtypes"})
public class OracleSqlClientItest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	protected static LoggingDeploymentExecutionContext context;

	protected static ItestHost ec2host;
	protected static DeployitTester tester;

	protected Host host;
	protected Container container;
	Environment environment;

	@BeforeClass
	public static void setupEc2Host() {
		PluginBooter.bootWithoutGlobalContext();
		tester = DeployitTester.build();

		ec2host = getItestHost("ora-unix");
		ec2host.setup();
		context = new LoggingDeploymentExecutionContext(OracleSqlClientItest.class);
	}

	@AfterClass
	public static void teardownEc2Host() {
		ec2host.teardown();
	}

	@Before
	public void setup() throws IOException {
		host = newInstance("overthere.SshHost");
		host.setId("Infrastructure/ora-unix");
		host.setOs(UNIX);
		host.setProperty(CONNECTION_TYPE, SFTP);
		host.setProperty(ADDRESS, ec2host.getHostName());
		host.setProperty(USERNAME, "ec2-user");
		host.setProperty("privateKeyFile", createPrivateKeyFile());

		container = newInstance("sql.OracleClient");
		container.setId("/Infrastructure/itestServer");
		container.setHost(host);
		container.setProperty("oraHome", "/usr/lib/oracle/xe/app/oracle/product/10.2.0/server");
		container.setProperty("schema", "XE");
		container.setProperty("username", "SYSTEM");
		container.setProperty("password", "deployit");

		environment = createEnvironment(container);
	}

    @Test
	public void deployUndeploySqlFolder() throws IOException {
		Folder sqlScriptFolderV1 = createArtifact("sqlScripts", "1.0", "ora_sqlscripts_v1", "sql.SqlScripts", folder.getRoot());
		DeploymentPackage deploymentPackageV1 = createDeploymentPackage("1.0", sqlScriptFolderV1);
		ExecutedFolder<Folder> executedSqlScriptsV1 = (ExecutedFolder<Folder>) tester.generateDeployed(sqlScriptFolderV1, container, Type.valueOf("sql.ExecutedSqlScripts"));
		DeployedApplication appV1 = newDeployedApplication("PetClinic", "1.0", sqlScriptFolderV1);
		assertDeploy(deploymentPackageV1, environment, executedSqlScriptsV1);

		Folder sqlScriptFolderV2 = createArtifact("sqlScripts", "2.0", "ora_sqlscripts_v2", "sql.SqlScripts", folder.getRoot());
		DeploymentPackage deploymentPackageV2 = createDeploymentPackage("2.0", sqlScriptFolderV2);
		ExecutedFolder<Folder> executedSqlScriptsV2 = (ExecutedFolder<Folder>) tester.generateDeployed(sqlScriptFolderV2, container, Type.valueOf("sql.ExecutedSqlScripts"));
		DeployedApplication appV2 = newDeployedApplication("PetClinic", "2.0", sqlScriptFolderV2);
		assertUpgrade(deploymentPackageV2, appV1, executedSqlScriptsV1, executedSqlScriptsV2);
		
		assertUndeploy(appV2, executedSqlScriptsV2);
	}

    private void assertDeploy(DeploymentPackage deploymentPackage, Environment environment, Deployed deployed) {
		DeltaSpecification spec = new DeltaSpecificationBuilder().initial(deploymentPackage, environment).create(deployed).build();
		Plan resolvedPlan = tester.resolvePlan(spec);
		List<DeploymentStep> resolvedSteps = resolvedPlan.getSteps();
		assertThat(resolvedSteps.size(), is(3));
		Step.Result result = tester.executePlan(resolvedPlan, context);
		assertThat(result, is(Step.Result.Success));
	}

	private void assertUpgrade(Version newVersion, DeployedApplication deployedApp, Deployed previousDeployedArtifact, Deployed deployedArtifact) {
		DeltaSpecification spec = new DeltaSpecificationBuilder().upgrade(newVersion, deployedApp).modify(previousDeployedArtifact, deployedArtifact).build();
		Plan resolvedPlan = tester.resolvePlan(spec);
		List<DeploymentStep> resolvedSteps = resolvedPlan.getSteps();
		assertThat(resolvedSteps.size(), greaterThan(0));
		Step.Result result = tester.executePlan(resolvedPlan, context);
		assertThat(result, is(Step.Result.Success));
	}

	private void assertUndeploy(DeployedApplication deployedApp, Deployed previousDeployedArtifact) {
		DeltaSpecification spec = new DeltaSpecificationBuilder().undeploy(deployedApp).destroy(previousDeployedArtifact).build();
		Plan resolvedPlan = tester.resolvePlan(spec);
		List<DeploymentStep> resolvedSteps = resolvedPlan.getSteps();
		System.out.println(resolvedSteps);
		assertThat(resolvedSteps.size(), is(4));
		Step.Result result = tester.executePlan(resolvedPlan, context);
		assertThat(result, is(Step.Result.Success));
	}

	protected DeployedApplication newDeployedApplication(String name, String version, Deployable... deployables) {
		DeploymentPackage pkg = createDeploymentPackage(version, deployables);
		Environment env = createEnvironment(container);
		DeployedApplication deployedApp = newInstance("udm.DeployedApplication");
		deployedApp.setVersion(pkg);
		deployedApp.setEnvironment(env);
		deployedApp.setId(id(env.getId(), name));
		return deployedApp;
	}

	public static <T extends SourceArtifact> Folder createArtifact(String name, String version, String classpathResource, String type, File workingFolder)
	        throws IOException {
		Folder folder = newInstance(type);
		folder.setId("Applications/Test/" + version + "/" + name);
		URL artifactURL = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		folder.setFile(LocalFile.valueOf(new File(artifactURL.getFile())));
		return folder;

	}

	private static String createPrivateKeyFile() {
		try {
			final File privateKeyFile = File.createTempFile("private", ".key");
			privateKeyFile.deleteOnExit();
			CharStreams.copy(newReaderSupplier(getResource("xebialabs-itests.pem"), Charset.defaultCharset()), new OutputSupplier<Writer>() {
				public Writer getOutput() throws IOException {
					return new FileWriter(privateKeyFile);
				}
			});
			return privateKeyFile.getAbsolutePath();

		} catch (Exception e) {
			throw new RuntimeIOException("Cannot generate private key file", e);
		}
	}

}
