package com.xebialabs.deployit.plugin.cloudburst;

import static com.xebialabs.deployit.ci.Host.getLocalHost;
import static com.xebialabs.deployit.ci.HostAccessMethod.SSH_SFTP;
import static com.xebialabs.deployit.ci.OperatingSystemFamily.UNIX;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.clearStepExecutionContext;
import static com.xebialabs.deployit.test.support.utils.ItestUtils.resourceToFile;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.springframework.core.io.ClassPathResource;

import com.xebialabs.deployit.ci.Host;
import com.xebialabs.deployit.ci.artifact.Ear;
import com.xebialabs.deployit.plugin.cloudburst.CloudBurstAppliance;
import com.xebialabs.deployit.plugin.was.ci.WasUnmanagedServer;
import com.xebialabs.deployit.plugin.was.ci.WasVersion;

public abstract class CloudBurstTestBase {

	protected CloudBurstAppliance appliance;

	protected String patternName;

	protected String cloudGroupName;

	protected String systemName;

	protected String systemPassword;

	protected String hostname;

	protected WasUnmanagedServer server1;

	protected Ear ear;

	@Before
	public void setupCloudBurstTestData() throws IOException {
		// Information about the appliance
		appliance = new CloudBurstAppliance();
		appliance.setLabel("Infrastructure/wcacli/wca");
		appliance.setCliHost(getLocalHost());
		appliance.setCliHome("/Users/vinny/bin/cloudburst.cli");
		appliance.setAddress("wca");
		appliance.setUsername("cbadmin");
		appliance.setPassword("ingtec1");

		// Parameters to use when deploying a pattern
		patternName = "IIC - WebSphere single server for Deployit";
		cloudGroupName = "IIC_MANAGED_GRP";
		systemName = "IICDeployit";
		systemPassword = "iicu5er";

		// Information about the virtual system that is created when the pattern is deployed
		hostname = "iic-vrh5-cb0.amsiic.ibm.com";
		Host virtualSystemHost = new Host();
		virtualSystemHost.setLabel(hostname);
		virtualSystemHost.setAddress(hostname);
		virtualSystemHost.setUsername("root");
		virtualSystemHost.setPassword(systemPassword);
		virtualSystemHost.setOperatingSystemFamily(UNIX);
		virtualSystemHost.setAccessMethod(SSH_SFTP);
		server1 = new WasUnmanagedServer();
		server1.setHost(virtualSystemHost);
		server1.setLabel(hostname + "/server1");
		server1.setUsername("virtuser");
		server1.setPassword(systemPassword);
		server1.setWasHome("/opt/IBM/WebSphere/Profiles/DefaultAppSrv01");
		server1.setPort(8880);
		// N.B.: The fields below are normally filled in by the discovery
		server1.setName("server1");
		server1.setCellName("CloudBurstCell_1");
		server1.setNodeName("CloudBurstNode_1");
		server1.setVersion(WasVersion.WAS_70);

		// The EAR to be deployed
		ear = new Ear();
		ear.setLabel("PetClinic 1.0 - ear");
		ear.setName("petclinic-ear");
		ClassPathResource petclinic10earAsResource = new ClassPathResource("com/xebialabs/deployit/test/support/artifacts/PetClinic-1.0.ear");
		ear.setLocation(resourceToFile(petclinic10earAsResource).getPath());
	}

	@After
	public void cleanup() {
		clearStepExecutionContext();
	}

}
