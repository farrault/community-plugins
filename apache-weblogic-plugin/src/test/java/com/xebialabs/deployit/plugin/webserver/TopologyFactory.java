package com.xebialabs.deployit.plugin.webserver;

import com.google.common.collect.Sets;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugin.wls.container.*;

import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

public class TopologyFactory {
	public static Host wls10gUnixHost, wls10gUnixHost2, apacheUnixHost;
	public static Domain wls10gUnixDomain;
	public static Cluster wls10gUnixCluster1, wls10gUnixCluster2, wls11gWinCluster1, wls11gWinCluster2;
	public static Server wls10gUnixServer1, wls10gUnixServer2, wls11gWinServer1, wls11gWinServer2;
	public static JmsServer wls10gUnixJmsServer1, wls10gUnixJmsServer2, wls11gWinJmsServer1, wls11gWinJmsServer2;

	public static Container apacheServer;

	static {
		initialize10gUnixTopology();
	}

	static void initialize10gUnixTopology() {
		wls10gUnixHost = newInstance("overthere.SshHost");
		wls10gUnixHost.setId("Infrastructure/wls-103");
		wls10gUnixHost.setOs(UNIX);
		wls10gUnixHost.setProperty(CONNECTION_TYPE, SFTP);
		wls10gUnixHost.setProperty(ADDRESS, "wls-101");
		wls10gUnixHost.setProperty(USERNAME, "root");
		wls10gUnixHost.setProperty(PASSWORD, "centos");

		wls10gUnixHost2 = newInstance("overthere.SshHost");
		wls10gUnixHost2.setId("Infrastructure/wls-103");
		wls10gUnixHost2.setOs(UNIX);
		wls10gUnixHost2.setProperty(CONNECTION_TYPE, SFTP);
		wls10gUnixHost2.setProperty(ADDRESS, "wls-102");
		wls10gUnixHost2.setProperty(USERNAME, "root");
		wls10gUnixHost2.setProperty(PASSWORD, "centos");


		wls10gUnixDomain = newInstance(Domain.class);
		wls10gUnixDomain.setId("Infrastructure/wls-103/adDomain");
		wls10gUnixDomain.setUsername("weblogic");
		wls10gUnixDomain.setPassword("weblogic");
		wls10gUnixDomain.setWlHome("/opt/bea-10.3/wlserver_10.3");
		wls10gUnixDomain.setDomainHome("/opt/bea-10.3/user_projects/domains/adDomain");
		wls10gUnixDomain.setHost(wls10gUnixHost);
		wls10gUnixDomain.setStartMode(StartMode.Script);

		wls10gUnixServer1 = newInstance(Server.class);
		wls10gUnixServer1.setId(wls10gUnixDomain.getId() + "/wlserver-1");
		wls10gUnixServer1.setPort(7009);
		wls10gUnixServer1.setHost(wls10gUnixHost);
		wls10gUnixServer1.setDomain(wls10gUnixDomain);
		wls10gUnixServer1.setStartCommand("nohup /opt/bea-10.3/user_projects/domains/adDomain/bin/startManagedWebLogic.sh wlserver-1 &");

		wls10gUnixCluster1 = newInstance(Cluster.class);
		wls10gUnixCluster1.setId(wls10gUnixDomain.getId() + "/Cluster-1");
		wls10gUnixCluster1.setDomain(wls10gUnixDomain);
		wls10gUnixCluster1.setServers(Sets.newHashSet(wls10gUnixServer1));

		wls10gUnixServer2 = newInstance(Server.class);
		wls10gUnixServer2.setId(wls10gUnixDomain.getId() + "/wlserver-2");
		wls10gUnixServer2.setPort(7010);
		wls10gUnixServer2.setHost(wls10gUnixHost2);
		wls10gUnixServer2.setDomain(wls10gUnixDomain);

		wls10gUnixCluster2 = newInstance(Cluster.class);
		wls10gUnixCluster2.setId(wls10gUnixDomain.getId() + "/Cluster-2");
		wls10gUnixCluster2.setDomain(wls10gUnixDomain);
		wls10gUnixCluster2.setServers(Sets.newHashSet(wls10gUnixServer2));

		wls10gUnixJmsServer1 = newInstance(JmsServer.class);
		wls10gUnixJmsServer1.setId(wls10gUnixServer1.getId() + "/existingJmsServer1");
		wls10gUnixJmsServer1.setServer(wls10gUnixServer1);

		wls10gUnixJmsServer2 = newInstance(JmsServer.class);
		wls10gUnixJmsServer2.setId(wls10gUnixServer2.getId() + "/existingJmsServer2");
		wls10gUnixJmsServer2.setServer(wls10gUnixServer2);

		wls10gUnixDomain.addClusters(wls10gUnixCluster1);
		wls10gUnixDomain.addClusters(wls10gUnixCluster2);


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