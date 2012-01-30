package com.xebialabs.deployit.plugin.rpm;


import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.test.support.TestUtils;
import com.xebialabs.overthere.OperatingSystemFamily;

import static com.xebialabs.deployit.test.support.TestUtils.id;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

public class TopologyFactory {
	public static Host sshHost;
	public static Container rpmContainer;

	static {
		initializeTopology();
	}

	static void initializeTopology() {
		sshHost = TestUtils.newInstance("overthere.SshHost");
		sshHost.setId(id("Infrastructure", "aHost"));
		sshHost.setOs(OperatingSystemFamily.UNIX);
		sshHost.setProperty(CONNECTION_TYPE, SFTP);
		sshHost.setProperty("address", "ora-10g-express-unix");
		sshHost.setProperty("username", "root");
		sshHost.setProperty("password", "centos");
		sshHost.setProperty("tmpDeleteOnDisconnect", false);

		rpmContainer = TestUtils.newInstance("rpm.Container");
		rpmContainer.setId(id("Infrastructure", "aHost", "aHostC"));
		rpmContainer.setProperty("host", sshHost);


	}


}