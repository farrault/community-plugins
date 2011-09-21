/*
 * Copyright (c) 2008-2011 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.deployit.plugin.apache.modjk.runbook;

import com.google.common.collect.Sets;
import com.xebialabs.deployit.ci.*;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfigurationMapping;
import com.xebialabs.deployit.plugin.jbossas.ci.JbossasServer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

public class JBossModJKRunBookTest extends BaseTestForModJKRunBook {

	protected Deployment deployment;
	protected Deployment deployment2;
	protected Deployment deployment3;
	private ModJkApacheModuleConfigurationMapping mapping;

	@Before
	public void setup() throws IOException {

		mapping = new ModJkApacheModuleConfigurationMapping();
		mapping.setLabel("Mapping Of MyApplication");
		mapping.setSource(jkConfiguration);
		mapping.setTarget(apacheHttpdServer);
		mapping.setVirtualHost("my.pet.local:8890");


		ModJkApacheModuleConfigurationMapping mapping2 = new ModJkApacheModuleConfigurationMapping();
		mapping2.setLabel("MappingOfMyApplication2");
		mapping2.setSource(newJkConfiguration("/myURL2"));
		mapping2.setTarget(apacheHttpdServer);
		mapping2.setVirtualHost("my.pet2.local:8891");

		ModJkApacheModuleConfigurationMapping mapping3 = new ModJkApacheModuleConfigurationMapping();
		mapping3.setLabel("MappingOfMyApplication3");
		mapping3.setSource(newJkConfiguration("/myURL3"));
		mapping3.setTarget(apacheHttpdServer);
		mapping3.setVirtualHost("my.pet3.local:8880");


		JbossasServer as1 = new JbossasServer();
		as1.setLabel("Tomcatas1");
		as1.setAjpPort(8009);
		as1.setHost(host1);

		JbossasServer as2 = new JbossasServer();
		as2.setLabel("Tomcatas2");
		as2.setAjpPort(8010);
		as2.setHost(host1);

		JbossasServer as3 = new JbossasServer();
		as3.setLabel("Tomcatas3");
		as3.setAjpPort(8009);
		as3.setHost(host2);

		JbossasServer as4 = new JbossasServer();
		as4.setLabel("Tomcatas4");
		as4.setAjpPort(8010);
		as4.setHost(host2);


		Set<LoadBalancedServerAware> ass = Sets.newHashSet();
		ass.add(as1);
		ass.add(as2);

		Set<LoadBalancedServerAware> ass2 = Sets.newHashSet();
		ass2.add(as3);
		ass2.add(as4);

		mapping.setTargets(ass);
		mapping2.setTargets(ass);
		mapping3.setTargets(ass2);

		Environment e = new Environment();
		e.addMember(host1);
		e.addMember(apacheHttpdServerModJk);
		e.addMember(as1);
		e.addMember(as2);

		Environment e2 = new Environment();
		e.addMember(host1);
		e.addMember(apacheHttpdServerModJk);
		e.addMember(as3);
		e.addMember(as4);

		DeploymentPackage dp = new DeploymentPackage();
		dp.setLabel("DP-TEST");
		dp.setApplication(new Application());
		dp.addMiddlewareResource(jkConfiguration);

		DeploymentPackage dp2 = new DeploymentPackage();
		dp2.setLabel("DP-TEST");
		dp2.setApplication(new Application());
		dp2.addMiddlewareResource(jkConfiguration);

		deployment = new Deployment();
		deployment.setSource(dp);
		deployment.setTarget(e);
		deployment.addMapping(mapping);

		deployment2 = new Deployment();
		deployment2.setSource(dp2);
		deployment2.setTarget(e);
		deployment2.addMapping(mapping2);


		deployment3 = new Deployment();
		deployment3.setSource(dp);
		deployment3.setTarget(e2);
		deployment3.addMapping(mapping3);
	}


	@Test
	public void testConfigurationWithJkManager() throws Exception {
		deployModJkConfiguration(deployment);
		assertContains(getModJKConfiguration(), "JkLogFile", "JkShmFile", "JkLogLevel", "JkLogStampFormat");
		assertNotContains(getModJKConfiguration(), "JkOptions");
		assertNotContains(getModJKConfiguration(), "JkOptions");

		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), "jkstatus", "Tomcatas2,Tomcatas1", mapping.getVirtualHost().replace(':', '_'));

		undeplyModJkConfiguration(deployment);
	}

	@Test
	public void testConfigurationWithoutJkManager() throws Exception {
		jkConfiguration.setJkstatus(false);
		deployModJkConfiguration(deployment);
		assertContains(getModJKConfiguration(), "JkLogFile", "JkShmFile", "JkLogLevel", "JkLogStampFormat");
		assertNotContains(getModJKConfiguration(), "jkmanager");
		assertNotContains(getModJKConfiguration(), "JkOptions");

		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), mapping.getVirtualHost().replace(':', '_'));
		assertNotContains(apacheHttpdServerModJk.getJkWorkerFile(), "jkstatus");


		final HostSession session = apacheHttpdServer.connectToAdminHost();
		assertContains(getVirtualHostConfigFilePath(session, apacheHttpdServer, mapping), "JkMount /myURL LB-my.pet.local_8890");
		session.close();

		undeplyModJkConfiguration(deployment);
	}

	@Test
	public void testModJKWithJKOptions() throws Exception {
		jkConfiguration.setJkOptions("+JKTRUC");
		deployModJkConfiguration(deployment);

		assertContains(getModJKConfiguration(), "JkLogFile", "JkShmFile", "JkLogLevel", "JkLogStampFormat", "JkOptions");
		undeplyModJkConfiguration(deployment);
	}

	@Test
	public void testModJKWithLoadModule() throws Exception {
		apacheHttpdServerModJk.setModulePath("/path/to/modjk.so");
		deployModJkConfiguration(deployment);
		assertContains(getModJKConfiguration(), "JkLogFile", "JkShmFile", "JkLogLevel", "JkLogStampFormat", "LoadModule");
		undeplyModJkConfiguration(deployment);
	}


	@Test
	public void testWithWorkerOption() throws Exception {
		jkConfiguration.setCacheSize(10);
		jkConfiguration.setCacheTimeout(23);
		deployModJkConfiguration(deployment);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), "worker.Tomcatas2.cachesize=10", "worker.Tomcatas1.cache_timeout=23");
		undeplyModJkConfiguration(deployment);
	}

	@Test
	public void testWithWorkerOptionSocket() throws Exception {
		jkConfiguration.setSocketKeepAlive(true);
		jkConfiguration.setSocketTimeout(100);
		deployModJkConfiguration(deployment);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), ".socket_keepalive=1", ".socket_timeout=100", ".sticky_session=1");
		undeplyModJkConfiguration(deployment);
	}

	@Test
	public void testWithWorkerNoStickySession() throws Exception {
		jkConfiguration.setStickySession(false);
		deployModJkConfiguration(deployment);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), "sticky_session=0");
		undeplyModJkConfiguration(deployment);
	}


	@Test
	public void testTwoDeployments() throws Exception {


		final String balanceWorker1 = "worker.LB-my.pet.local_8890.balance_workers=Tomcatas2,Tomcatas1";
		final String balanceWorker2 = "worker.LB-my.pet2.local_8891.balance_workers=Tomcatas2,Tomcatas1";

		deployModJkConfiguration(deployment);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1);
		assertNotContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker2);

		deployModJkConfiguration(deployment2);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1, balanceWorker2);

		undeplyModJkConfiguration(deployment);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker2);
		assertNotContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1);

		undeplyModJkConfiguration(deployment2);

		assertDirEmpty();

	}

	@Test
	public void testTwoDeployment2() throws Exception {


		final String balanceWorker1 = "worker.LB-my.pet.local_8890.balance_workers=Tomcatas2,Tomcatas1";
		final String balanceWorker2 = "worker.LB-my.pet2.local_8891.balance_workers=Tomcatas2,Tomcatas1";


		deployModJkConfiguration(deployment);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1);
		assertNotContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker2);

		deployModJkConfiguration(deployment2);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1, balanceWorker2);


		undeplyModJkConfiguration(deployment2);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1);
		assertNotContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker2);

		undeplyModJkConfiguration(deployment);

		assertDirEmpty();

	}

	@Test
	public void testTwoDeployment3() throws Exception {


		final String balanceWorker1 = "worker.LB-my.pet3.local_8880.balance_workers=Tomcatas4,Tomcatas3";
		final String balanceWorker2 = "worker.LB-my.pet.local_8890.balance_workers=Tomcatas2,Tomcatas1";


		deployModJkConfiguration(deployment3);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1);
		assertNotContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker2);


		deployModJkConfiguration(deployment);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1, balanceWorker2);


		undeplyModJkConfiguration(deployment3);
		assertContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker2);
		assertNotContains(apacheHttpdServerModJk.getJkWorkerFile(), balanceWorker1);

		undeplyModJkConfiguration(deployment);

		assertDirEmpty();

	}

	private void assertDirEmpty() {
		assertTrue("Target directory is not empty", tempDir.isDirectory() && tempDir.list().length == 0);
	}

}
