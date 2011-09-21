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

import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.ChangePlan;
import com.xebialabs.deployit.ChangeResolution;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.Deployment;
import com.xebialabs.deployit.ci.Host;
import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.plugin.apache.httpd.ci.ApacheHttpdServer;
import com.xebialabs.deployit.plugin.apache.httpd.step.ApacheVirtualHostDefinition;
import com.xebialabs.deployit.plugin.apache.httpd.step.RestartApacheHttpdServerStep;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModule;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfiguration;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfigurationMapping;
import com.xebialabs.deployit.plugin.apache.modjk.step.CreateModkJKConfigFileStep;
import com.xebialabs.deployit.plugin.apache.modjk.step.DeleteModkJKConfigFileStep;
import com.xebialabs.deployit.test.support.stubs.StubChange;
import com.xebialabs.deployit.test.support.stubs.StubChangePlan;
import com.xebialabs.deployit.test.support.utils.DebugStepExecutionContext;
import com.xebialabs.deployit.test.support.utils.RunBookTestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class BaseTestForModJKRunBook {


	protected ModJkApacheModuleRunbook runbook;
	protected File tempDir;
	protected ModJkApacheModuleConfiguration jkConfiguration;
	protected ModJkApacheModule apacheHttpdServerModJk;
	protected ApacheHttpdServer apacheHttpdServer;

	protected Host host1;
	protected Host host2;

	@Before
	public void setupBase() throws IOException {
		tempDir = new File(File.createTempFile("jboss", "temp").getParentFile(), "jk");
		tempDir.mkdirs();


		for (File f : tempDir.listFiles())
			f.delete();

		runbook = new ModJkApacheModuleRunbook();

		jkConfiguration = new ModJkApacheModuleConfiguration();
		jkConfiguration.setJkstatus(true);
		jkConfiguration.setUrlMounts("/myURL");

		apacheHttpdServer = new ApacheHttpdServer();
		apacheHttpdServer.setLabel("apacheHttpdServer");
		apacheHttpdServer.setHost(Host.getLocalHost());
		apacheHttpdServer.setConfigurationLocation(tempDir.getAbsolutePath());
		apacheHttpdServer.setHtdocsLocation("/apache/jk/weblogic/www");
		apacheHttpdServer.setErrorLogLocation("/apache/jk/log/apache2/error.log");
		apacheHttpdServer.setAccessLogLocation("/apache/jk/log/apache2/access.log");
		apacheHttpdServer.setApachectlPath("/usr/sbin/apache2ctl");

		apacheHttpdServerModJk = new ModJkApacheModule();
		apacheHttpdServerModJk.setJkLogFile(new File(tempDir, "log/file").getAbsolutePath());
		apacheHttpdServerModJk.setJkShmFile("/tmp/jk/shm/file");
		apacheHttpdServerModJk.setLabel("jkConfiguration");
		apacheHttpdServerModJk.setModuleName("mod_jk");
		apacheHttpdServerModJk.setJkWorkerFile(new File(tempDir, "workers.properties").getAbsolutePath());

		apacheHttpdServer.addModule(apacheHttpdServerModJk);


		host1 = new Host();
		host1.setAddress("a.b.c.d");
		host1.setOperatingSystemFamily(OperatingSystemFamily.UNIX);

		host2 = new Host();
		host2.setAddress("168.156.2.12");
		host2.setOperatingSystemFamily(OperatingSystemFamily.UNIX);


	}


	protected void undeplyModJkConfiguration(Deployment revision) {
		Change<Deployment> deploymentChange = new StubChange<Deployment>(revision, null);
		ChangePlan cp = new StubChangePlan(deploymentChange);
		Collection<ChangeResolution> crs = runbook.resolve(cp);
		List<Step> steps = RunBookTestUtils.assertOneResolutionAndGetItsSteps(crs);
		RunBookTestUtils.assertTypeSequence(steps, DeleteModkJKConfigFileStep.class, RestartApacheHttpdServerStep.class);
		Step step = steps.iterator().next();


		step.execute(new DebugStepExecutionContext());

	}

	protected void deployModJkConfiguration(Deployment revision) {
		Change<Deployment> deploymentChange = new StubChange<Deployment>(null, revision);
		ChangePlan cp = new StubChangePlan(deploymentChange);
		Collection<ChangeResolution> crs = runbook.resolve(cp);
		List<Step> steps = RunBookTestUtils.assertOneResolutionAndGetItsSteps(crs);
		RunBookTestUtils.assertTypeSequence(steps, CreateModkJKConfigFileStep.class, RestartApacheHttpdServerStep.class);
		CreateModkJKConfigFileStep step = (CreateModkJKConfigFileStep) steps.iterator().next();


		step.execute(new DebugStepExecutionContext());

	}

	protected void assertContains(String configFilePath, String... keywords) {
		Map<String, Boolean> keywordMap = new HashMap<String, Boolean>();
		for (String k : keywords)
			keywordMap.put(k, Boolean.FALSE);
		int initialSize = keywordMap.size();


		final HostSession session = Host.getLocalHost().getHostSession();
		StringBuffer fullContent = new StringBuffer();
		try {
			final InputStream inputStream = session.getFile(configFilePath).get();

			fullContent.append(new String(IOUtils.toByteArray(inputStream)));
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			session.close();
		}

		String[] lines = fullContent.toString().split("\n");
		for (String line : lines) {
			for (Map.Entry<String, Boolean> e : keywordMap.entrySet())
				if (line.contains(e.getKey()))
					e.setValue(Boolean.TRUE);
		}

		assertEquals(initialSize, keywordMap.size());
		boolean allFound = Boolean.TRUE;
		for (Boolean value : keywordMap.values()) {
			allFound = allFound && value;
		}

		assertTrue("From " + configFilePath + ", Not all keywords has not been found in " + keywordMap, allFound);


	}

	protected void assertNotContains(String configFilePath, String... keywords) {
		Map<String, Boolean> keywordMap = new HashMap<String, Boolean>();
		for (String k : keywords)
			keywordMap.put(k, Boolean.FALSE);
		int initialSize = keywordMap.size();


		final HostSession session = Host.getLocalHost().getHostSession();
		StringBuffer fullContent = new StringBuffer();
		try {
			final InputStream inputStream = session.getFile(configFilePath).get();

			fullContent.append(new String(IOUtils.toByteArray(inputStream)));
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			session.close();
		}

		String[] lines = fullContent.toString().split("\n");
		for (String line : lines) {
			for (Map.Entry<String, Boolean> e : keywordMap.entrySet())
				if (line.contains(e.getKey()))
					e.setValue(Boolean.TRUE);
		}

		assertEquals(initialSize, keywordMap.size());
		boolean allFound = Boolean.FALSE;
		for (Boolean value : keywordMap.values()) {
			allFound = allFound || value;
		}

		assertTrue("From " + configFilePath + ", Not all keywords has not been found in " + keywordMap, !allFound);


	}

	protected String getModJKConfiguration() {
		return apacheHttpdServer.getConfigurationLocation() + apacheHttpdServer.getHost().getOperatingSystemFamily().getFileSeparator() + "mod_jk.conf";
	}

	protected String getVirtualHostConfigFilePath(HostSession hostSession, ApacheHttpdServer apacheHttpdServer, ModJkApacheModuleConfigurationMapping mapping) {
		String configFileWritePath;
		ApacheVirtualHostDefinition definition = new ApacheVirtualHostDefinition(mapping.getVirtualHost(), apacheHttpdServer.getHtdocsLocation(),
				apacheHttpdServer.getHost().getOperatingSystemFamily(), null);
		String configLocationWrite = apacheHttpdServer.getConfigurationLocation();
		HostFile configLocationWriteFile = hostSession.getFile(configLocationWrite);
		if (configLocationWriteFile.isDirectory()) {
			configFileWritePath = configLocationWrite + hostSession.getHostOperatingSystem().getFileSeparator() + definition.getFileName();
		} else {
			configFileWritePath = configLocationWrite;
		}
		return configFileWritePath;
	}

	protected ModJkApacheModuleConfiguration newJkConfiguration(String urlPrefix) {
		ModJkApacheModuleConfiguration config = new ModJkApacheModuleConfiguration();
		config.setUrlMounts(urlPrefix);
		return config;
	}
}
