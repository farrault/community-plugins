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

package com.xebialabs.deployit.plugin.apache.httpd.step;

import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.plugin.apache.httpd.ci.ApacheHttpdServer;

/**
 * Creates a file with a VirtualHost definition for Apache Httpd.
 */
@SuppressWarnings("serial")
public class CreateApacheHttpdVirtualHostStep extends AbstractApacheHttpdServerStep {

	private String virtualHost;

	private Collection<? extends Object> context;

	public CreateApacheHttpdVirtualHostStep(ApacheHttpdServer apacheWebServer, String virtualHost, Collection<?> context) {
		super(apacheWebServer);

		if (StringUtils.isEmpty(virtualHost)) {
			throw new IllegalArgumentException("The virtual host definition cannot be empty");
		}

		this.virtualHost = virtualHost;
		this.context = context;
		setDescription("Create Apache HTTP Server virtualhost " + virtualHost + " on host " + apacheWebServer.getHost());
	}

	public boolean execute(StepExecutionContext ctx) {
		HostSession hostSession = apacheHttpdServer.connectToAdminHost();

		ApacheVirtualHostDefinition definition = getApacheVirtualHostDefinition();

		// write apache config
		try {
			String configFileWritePath;
			String configLocationWrite = apacheHttpdServer.getConfigurationLocation();
			HostFile configLocationWriteFile = hostSession.getFile(configLocationWrite);
			if (configLocationWriteFile.isDirectory()) {
				configFileWritePath = configLocationWrite + hostSession.getHostOperatingSystem().getFileSeparator() + definition.getFileName();
			} else {
				configFileWritePath = configLocationWrite;
			}

			ctx.logOutput("Creating Apache HTTPD VirtualHost configuration file at " + configFileWritePath);
			HostFile vhostConfFile = hostSession.getFile(configFileWritePath);
			String virtualHostDefinition = definition.toVirtualHostDefinition();
			vhostConfFile.put(IOUtils.toInputStream(virtualHostDefinition), virtualHostDefinition.length());

			HostFile documentRootPath = hostSession.getFile(definition.getDocumentRoot());
			if (!documentRootPath.exists()) {
				ctx.logOutput("Creating Apache HTTPD document root at " + documentRootPath);
				documentRootPath.mkdir();
			}
			return true;
		} finally {
			hostSession.close();
		}
	}

	protected ApacheVirtualHostDefinition getApacheVirtualHostDefinition() {
		ApacheVirtualHostDefinition definition = new ApacheVirtualHostDefinition(getDeployedOnVhostDefinition(), apacheHttpdServer.getHtdocsLocation(),
				apacheHttpdServer.getHost().getOperatingSystemFamily(), context);
		return definition;
	}

	public String getDeployedOnVhostDefinition() {
		return virtualHost;
	}

	public void setDeployedOnVhostDefinition(String deployedOnVhostDefinition) {
		this.virtualHost = deployedOnVhostDefinition;
	}

	public Collection<?> getContext() {
		return context;
	}

}
