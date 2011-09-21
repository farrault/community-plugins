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

package com.xebialabs.deployit.plugin.apache.modjk.step;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.plugin.apache.httpd.ci.ApacheHttpdServer;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfiguration;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfigurationMapping;
import org.apache.commons.io.IOUtils;

import java.util.List;
import java.util.Map;


public class CreateModkJKConfigFileStep extends BaseModkJKConfigFileStep implements Step {

	private static final String DEFAULT_VHOST_MODJK_PROPERTIES_TEMPLATE = "com/xebialabs/deployit/plugin/apache/modjk/apache_vhost.conf.vm";

	private String lbWorker;
	private List<String> jkMounts;
	private List<String> jkUnmounts;

	public CreateModkJKConfigFileStep(ApacheHttpdServer apacheHttpdServer, ModJkApacheModuleConfiguration pluginConfiguration, ModJkApacheModuleConfigurationMapping mapping) {
		super(pluginConfiguration, apacheHttpdServer, mapping);
		setDescription("Generate mod_jk Apache Plugin configuration file on " + this.apacheHttpdServer.getHost());

		jkMounts = pluginConfiguration.getUrlMounts();
		jkUnmounts = pluginConfiguration.getUrlUnmounts();
		this.lbWorker = loadbalancers.iterator().next().getName();
	}

	public boolean execute(StepExecutionContext ctx) {
		HostSession hostSession = apacheHttpdServer.connectToAdminHost();

		try {
			HostFile modjkConfigHostFile = hostSession.getFile(modkjkConfigFilePath);
			if (!modjkConfigHostFile.exists()) {
				ctx.logOutput("Generating mod_jk configuration file at " + modkjkConfigFilePath + " on " + apacheHttpdServer.getHost());
				String modJKConfiguration = resolveModJkPropertiesFile();
				modjkConfigHostFile.getParentFile().mkdirs();
				modjkConfigHostFile.put(IOUtils.toInputStream(modJKConfiguration), modJKConfiguration.length());
			}

			ctx.logOutput("Generating mod_jk workers for " + loadbalancerName + " in configuration file in " + apacheHttpdServer.getHost() + ":" + workerFile);
			loadWorkerContent(hostSession, true);
			String workersConfiguration = resolveWorkerPropertiesFile();
			HostFile workerConfigurationHostFile = hostSession.getFile(workerFile);
			workerConfigurationHostFile.getParentFile().mkdirs();
			workerConfigurationHostFile.put(IOUtils.toInputStream(workersConfiguration), workersConfiguration.length());

			ctx.logOutput("Generating apache configuration ");

			String configFileWritePath = getVirtualHostConfigFilePath(hostSession);
			ctx.logOutput("Creating Apache HTTPD VirtualHost configuration file at " + configFileWritePath);
			HostFile vhostConfFile = hostSession.getFile(configFileWritePath);
			String virtualHostDefinition = resolveVirtualHostApacheConfigurationFile();
			vhostConfFile.put(IOUtils.toInputStream(virtualHostDefinition), virtualHostDefinition.length());


			return true;
		}
		finally {
			hostSession.close();
		}
	}

	protected String resolveVirtualHostApacheConfigurationFile() {
		final Map<String, Object> map = getVelocityContext();
		map.put("apachevirtualhostdefinition", getApacheVirtualHostDefinition());
		map.put("apachehttpdserver", this.apacheHttpdServer);
		map.put("jkMounts", jkMounts);
		map.put("jkUnmounts", jkUnmounts);
		map.put("path", lbWorker);

		String evaluatedScript = generateWithVelocity(DEFAULT_VHOST_MODJK_PROPERTIES_TEMPLATE, map);

		if (logger.isDebugEnabled()) {
			logger.debug("virtual host configuration:" + evaluatedScript);
		}
		return evaluatedScript;
	}


}
