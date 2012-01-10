package com.xebialabs.deployit.plugin.webserver;


import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.test.support.TestUtils;
import com.xebialabs.overthere.OperatingSystemFamily;

import static com.xebialabs.deployit.test.support.TestUtils.id;
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

public class TopologyFactory {
	public static Host jbossHost1, jbossHost2, apacheUnixHost;
	public static Container jbossContainer1, jbossContainer2;
	public static Container apacheServer;

	static {
		initializeJBossUnixTopology();
	}

	static void initializeJBossUnixTopology() {
		jbossHost1 = TestUtils.newInstance("overthere.SshHost");
		jbossHost1.setId(id("Infrastructure", "aEC2Host1"));
		jbossHost1.setOs(OperatingSystemFamily.UNIX);
		jbossHost1.setProperty(CONNECTION_TYPE, SFTP);
		jbossHost1.setProperty("address", "ec2-46-137-66-55.eu-west-1.compute.amazonaws.com");
		jbossHost1.setProperty("username", "ec2-user");

		jbossHost2 = TestUtils.newInstance("overthere.SshHost");
		jbossHost2.setId(id("Infrastructure", "aEC2Host2"));
		jbossHost2.setOs(OperatingSystemFamily.UNIX);
		jbossHost2.setProperty(CONNECTION_TYPE, SFTP);
		jbossHost2.setProperty("address", "ec2-46-137-66-55.eu-west-2.compute.amazonaws.com");
		jbossHost2.setProperty("username", "ec2-user");


		jbossContainer1 = newInstance("jbossas.ServerV5");
		jbossContainer1.setId(id("Infrastructure", "aEC2Host1", "JBoss1"));
		jbossContainer1.setProperty("home", "/home/ec2-user/jboss-5.1.0.GA");
		jbossContainer1.setProperty("serverName", "standard");
		jbossContainer1.setStartWaitTime(0);
		jbossContainer1.setStopWaitTime(0);
		jbossContainer1.setHost(jbossHost1);

		jbossContainer2 = newInstance("jbossas.ServerV5");
		jbossContainer2.setId(id("Infrastructure", "aEC2Host2", "JBoss2"));
		jbossContainer2.setProperty("home", "/home/ec2-user/jboss-5.1.0.GA");
		jbossContainer2.setProperty("serverName", "standard");
		jbossContainer2.setStartWaitTime(0);
		jbossContainer2.setStopWaitTime(0);
		jbossContainer2.setHost(jbossHost2);

		apacheUnixHost = newInstance("overthere.LocalHost");
		apacheUnixHost.setId("Infrastructure/apache-22");
		apacheUnixHost.setOs(UNIX);

		apacheServer = newInstance("www.ApacheHttpdServer");
		apacheServer.setProperty("host", apacheUnixHost);
		apacheServer.setProperty("configurationFragmentDirectory", "/tmp/conf");
		apacheServer.setProperty("defaultDocumentRoot", "/tmp/doc");
		apacheServer.setProperty("stopCommand", "echo 'stop'");
		apacheServer.setProperty("startCommand", "echo 'start'");
		apacheServer.setProperty("startWaitTime", 0);
		apacheServer.setProperty("stopWaitTime", 0);

	}


}