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
import org.apache.log4j.Logger;

public class DeleteModkJKConfigFileStep extends BaseModkJKConfigFileStep implements Step {

	public DeleteModkJKConfigFileStep(ApacheHttpdServer webServer, ModJkApacheModuleConfiguration jkConfiguration, ModJkApacheModuleConfigurationMapping mapping) {
		super(jkConfiguration, webServer, mapping);
		setDescription("Remove mod_jk Apache Plugin configuration file on " + webServer.getHost());
	}

	public boolean execute(StepExecutionContext ctx) {
		HostSession hostSession = this.apacheHttpdServer.connectToAdminHost();
		try {
			ctx.logOutput("Removing mod_jk workers for " + loadbalancerName + " in configuration file in " + apacheHttpdServer.getHost() + ":" + workerFile);
			loadWorkerContent(hostSession, false);
			String workersConfiguration = resolveWorkerPropertiesFile();
			HostFile workerConfigurationHostFile = hostSession.getFile(workerFile);
			workerConfigurationHostFile.getParentFile().mkdirs();
			workerConfigurationHostFile.put(IOUtils.toInputStream(workersConfiguration), workersConfiguration.length());


			String configFileWritePath = getVirtualHostConfigFilePath(hostSession);
			ctx.logOutput("Removing Apache HTTPD VirtualHost configuration file at " + configFileWritePath);

			boolean result = hostSession.getFile(configFileWritePath).delete();

			if (this.workers.isEmpty() && this.loadbalancers.isEmpty()) {
				//No more loadbalancer configured, remove all files      *
				ctx.logOutput("No more loadbalancer configured, remove mod_jk configuration files");
				result = result && hostSession.getFile(workerFile).delete();
				//hostSession.getFile(mountConfigurationFilePath).delete();
				result = result && hostSession.getFile(modkjkConfigFilePath).delete();
			}

			return result;
		} finally {
			hostSession.close();
		}
	}

	private static Logger logger = Logger.getLogger(DeleteModkJKConfigFileStep.class);

}
