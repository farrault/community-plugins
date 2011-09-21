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

package com.xebialabs.deployit.plugin.apache.httpd.ci;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ResolutionException;
import com.xebialabs.deployit.ci.HttpdServer;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.hostsession.HostSessionFactory;
import com.xebialabs.deployit.plugin.apache.httpd.step.ApacheVirtualHostDefinition;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings("serial")
@ConfigurationItem(description = "An Apache 2 web server.", category = "middleware")
public class ApacheHttpdServer extends HttpdServer {

	@ConfigurationItemProperty(required = true, label = "Path to apachectl", description = "Path of the executable that will restart apache, e.g. /usr/sbin/apachectl")
	private String apachectlPath;

	@ConfigurationItemProperty(required = true, description = "Location where deployit will generate apache httpd.conf fragment files.")
	private String configurationLocation;

	@ConfigurationItemProperty(required = true, label = "Htdocs path", description = "Location where deployit will create a directory (based on the vhost name) where static content will be placed.")
	private String htdocsLocation;

	@ConfigurationItemProperty(required = true, description = "Location where deployit will create a directory where access log will be placed.")
	private String accessLogLocation;

	@ConfigurationItemProperty(required = true, description = "Location where deployit will create a directory where error log will be placed.")
	private String errorLogLocation;

	@ConfigurationItemProperty(asContainment = true)
	private Set<ApacheModule> modules = Sets.newHashSet();

	public HostSession connectToAdminHost() {
		return HostSessionFactory.getHostSession(host);
	}

	public ApacheVirtualHostDefinition getVhostDefinition(String virtualHost) {
		return new ApacheVirtualHostDefinition(virtualHost, htdocsLocation, host.getOperatingSystemFamily(), Collections.singleton(this));
	}

	public String getConfFilePathForVirtualHost(String virtualHost) {
		StringBuilder confFilePath = new StringBuilder();
		confFilePath.append(getConfigurationLocation());
		confFilePath.append(getHost().getFileSeparator());
		confFilePath.append(getVhostDefinition(virtualHost).getFileName());
		return confFilePath.toString();
	}

	public String getHtdocsDirPathForVirtualHost(String virtualHost) {
		return getVhostDefinition(virtualHost).getDocumentRoot();
	}

	public String getApachectlPath() {
		return apachectlPath;
	}

	public void setApachectlPath(String apachectlPath) {
		this.apachectlPath = apachectlPath;
	}

	public String getConfigurationLocation() {
		return configurationLocation;
	}

	public void setConfigurationLocation(String configurationLocation) {
		this.configurationLocation = configurationLocation;
	}

	public String getHtdocsLocation() {
		return htdocsLocation;
	}

	public void setHtdocsLocation(String htdocsLocation) {
		this.htdocsLocation = htdocsLocation;
	}

	public String getAccessLogLocation() {
		return accessLogLocation;
	}

	public void setAccessLogLocation(String accessLogLocation) {
		this.accessLogLocation = accessLogLocation;
	}

	public String getErrorLogLocation() {
		return errorLogLocation;
	}

	public void setErrorLogLocation(String errorLogLocation) {
		this.errorLogLocation = errorLogLocation;
	}

	public void addModule(ApacheModule module) {
		if (modules == null)
			modules = Sets.newHashSet();
		modules.add(module);
	}

	/**
	 * @param moduleclass
	 * @return the ApacheModule or null if not found in the apache configuration.
	 */
	public ApacheModule getModule(final Class<? extends ApacheModule> moduleclass) {
		final Set<ApacheModule> apacheModuleSet = Sets.filter(modules, new PredicateModuleByClass(moduleclass));
		if (!apacheModuleSet.isEmpty()) {
			return apacheModuleSet.iterator().next();
		}
		throw new ResolutionException("No module of type " + moduleclass + " in " + modules);
	}

	public boolean isSupportedModule(final Class<? extends ApacheModule> moduleclass) {
		return !Sets.filter(modules, new PredicateModuleByClass(moduleclass)).isEmpty();
	}

	public Set<ApacheModule> getModules() {
		return modules;
	}

	public void setModules(Set<ApacheModule> modules) {
		this.modules = modules;
	}

	private static class PredicateModuleByClass implements Predicate<ApacheModule> {

		final Class<? extends ApacheModule> moduleClass;

		public PredicateModuleByClass(Class<? extends ApacheModule> moduleClass) {
			this.moduleClass = moduleClass;
		}

		public boolean apply(ApacheModule input) {
			if (input == null)
				return false;
			return input.getClass().isAssignableFrom(moduleClass);
		}

	}
}
