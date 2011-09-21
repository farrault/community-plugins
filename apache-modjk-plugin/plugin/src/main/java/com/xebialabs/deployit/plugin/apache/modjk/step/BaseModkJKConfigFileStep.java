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
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.plugin.apache.httpd.ci.ApacheHttpdServer;
import com.xebialabs.deployit.plugin.apache.httpd.step.ApacheVirtualHostDefinition;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModule;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfiguration;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfigurationMapping;
import com.xebialabs.deployit.util.ExtendedStringUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.*;

public abstract class BaseModkJKConfigFileStep implements Step {

	private static final String DEFAULT_MODJK_PROPERTIES_TEMPLATE = "com/xebialabs/deployit/plugin/apache/modjk/mod_jk.properties.vm";

	private static final String DEFAULT_WORKER_PROPERTIES_TEMPLATE = "com/xebialabs/deployit/plugin/apache/modjk/workers.properties.vm";

	protected ModJkApacheModuleConfiguration pluginConfiguration;

	protected ApacheHttpdServer apacheHttpdServer;


	protected Set<Worker> workers;
	protected Set<LoadBalancer> loadbalancers = new HashSet<LoadBalancer>();

	protected String modkjkConfigFilePath;
	protected String loadbalancerName;

	private ModJkApacheModule modJkApacheModule;

	protected String description;
	protected String virtualHost;
	protected String workerFile;

	protected BaseModkJKConfigFileStep(ModJkApacheModuleConfiguration pluginConfiguration, ApacheHttpdServer apacheHttpdServer, ModJkApacheModuleConfigurationMapping mapping) {
		this.pluginConfiguration = pluginConfiguration;
		this.apacheHttpdServer = apacheHttpdServer;

		workers = mapping.getWorkers();
		loadbalancers.add(mapping.getLoadBalancer());
		modkjkConfigFilePath = this.apacheHttpdServer.getConfigurationLocation() + this.apacheHttpdServer.getHost().getOperatingSystemFamily().getFileSeparator() + "mod_jk.conf";

		loadbalancerName = mapping.getLoadBalancerName();

		modJkApacheModule = (ModJkApacheModule) this.apacheHttpdServer.getModule(ModJkApacheModule.class);
		if (modJkApacheModule == null) {
			throw new IllegalArgumentException("The targeted Apache Server is not configured with a Modjk Apache Module.");
		}
		workerFile = modJkApacheModule.getJkWorkerFile();
		virtualHost = mapping.getVirtualHost();

		if (StringUtils.isEmpty(virtualHost)) {
			throw new IllegalArgumentException("The virtual host definition cannot be empty.");
		}

	}

	protected void loadWorkerContent(HostSession hostSession, boolean createMode) {
		HostFile hostFile = hostSession.getFile(workerFile);

		Map<String, Worker> workers = new HashMap<String, Worker>();
		Map<String, LoadBalancer> loadBalancers = new HashMap<String, LoadBalancer>();

		parseWorkerPropertyFile(hostFile, workers, loadBalancers);

		//merge with existing workers/loadbalancers  attributes.
		if (createMode) {
			this.loadbalancers.addAll(loadBalancers.values());
			this.workers.addAll(workers.values());
		} else {
			//remove the deleted loadbalancers
			loadBalancers.values().removeAll(this.loadbalancers);
			this.loadbalancers.clear();
			this.loadbalancers.addAll(loadBalancers.values());

			workers.values().removeAll(this.workers);
			this.workers.clear();
			for (LoadBalancer lb : this.loadbalancers) {
				this.workers.addAll(lb.getWorkers());
			}
		}

	}


	private void parseWorkerPropertyFile(HostFile hostFile, Map<String, Worker> workers, Map<String, LoadBalancer> loadBalancers) {
		if (!hostFile.exists()) {
			return;
		}

		final InputStream inputStream = hostFile.get();

		Properties properties = new Properties();

		try {
			properties.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		//search for type (ajp13, lb)
		for (Object key : properties.keySet()) {
			String k = (String) key;
			if (!k.startsWith("worker."))
				continue;
			if (!k.endsWith(".type"))
				continue;
			String name = k.substring("worker.".length(), k.length() - ".type".length());
			String type = properties.getProperty(k);
			if ("lb".equals(type)) {
				loadBalancers.put(name, new LoadBalancer(name, properties));
			} else if ("ajp13".equals(type)) {
				//ajp13
				workers.put(name, new Worker(name, properties));
			}
		}

		//plug Workers on LoadBalancer
		for (Worker w : workers.values()) {
			for (LoadBalancer lb : loadBalancers.values()) {
				lb.addWorker(w);
			}
		}
	}


	protected String resolveModJkPropertiesFile() {
		String evaluatedScript = generateWithVelocity(DEFAULT_MODJK_PROPERTIES_TEMPLATE, getVelocityContext());

		if (logger.isDebugEnabled()) {
			logger.debug("mod_jk properties configuration:" + evaluatedScript);
		}
		return evaluatedScript;
	}

	protected String generateWithVelocity(String templatePath, Map<String, Object> velocityContext) {
		VelocityEngine ve = new VelocityEngine();
		try {
			ve.init();
		} catch (Exception exc) {
			throw new IllegalStateException("Cannot initialize Velocity templating engine", exc);
		}
		// populate context for velocity use

		VelocityContext vc = new VelocityContext(velocityContext);
		ClassPathResource scriptResource = new ClassPathResource(templatePath);
		InputStream scriptIn;
		try {
			scriptIn = scriptResource.getInputStream();
		} catch (IOException exc) {
			throw new IllegalArgumentException("Cannot read script template resource " + templatePath, exc);
		}

		String evaluatedScript;
		try {
			StringWriter evaluatedTemplateWriter = new StringWriter();
			ve.evaluate(vc, evaluatedTemplateWriter, " ", new InputStreamReader(scriptIn, "UTF-8"));
			evaluatedScript = evaluatedTemplateWriter.getBuffer().toString();
		} catch (IOException exc) {
			throw new RuntimeException("Cannot evaluate script template resource " + templatePath, exc);
		} finally {
			IOUtils.closeQuietly(scriptIn);
		}
		return evaluatedScript;
	}

	protected String resolveWorkerPropertiesFile() {
		String evaluatedScript = generateWithVelocity(DEFAULT_WORKER_PROPERTIES_TEMPLATE, getVelocityContext());

		if (logger.isDebugEnabled()) {
			logger.debug("worker properties configuration:" + evaluatedScript);
		}
		return evaluatedScript;
	}

	protected Map<String, Object> getVelocityContext() {
		Map<String, Object> velocityContext = new HashMap<String, Object>();
		velocityContext.put("workersFile", workerFile);
		velocityContext.put("workers", workers);
		velocityContext.put("loadbalancers", loadbalancers);
		velocityContext.put("loadbalancerNames", getLoadBalancerNames());
		velocityContext.put("pluginConfiguration", pluginConfiguration);
		velocityContext.put("modjk", modJkApacheModule);

		return velocityContext;
	}

	public String getLoadBalancerNames() {
		return ExtendedStringUtils.join(loadbalancers, new Transformer() {
			public String transform(Object obj) {
				LoadBalancer loadBalancer = (LoadBalancer) obj;
				if (loadBalancer == null)
					return "XXX";
				return loadBalancer.getName();
			}
		});

	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	protected String getVirtualHostConfigFilePath(HostSession hostSession) {
		String configFileWritePath;
		ApacheVirtualHostDefinition definition = getApacheVirtualHostDefinition();
		String configLocationWrite = apacheHttpdServer.getConfigurationLocation();
		HostFile configLocationWriteFile = hostSession.getFile(configLocationWrite);
		if (configLocationWriteFile.isDirectory()) {
			configFileWritePath = configLocationWrite + hostSession.getHostOperatingSystem().getFileSeparator() + definition.getFileName();
		} else {
			configFileWritePath = configLocationWrite;
		}
		return configFileWritePath;
	}

	protected ApacheVirtualHostDefinition getApacheVirtualHostDefinition() {
		return new ApacheVirtualHostDefinition(virtualHost, apacheHttpdServer.getHtdocsLocation(),
				apacheHttpdServer.getHost().getOperatingSystemFamily(), null);
	}

	static Logger logger = Logger.getLogger(BaseModkJKConfigFileStep.class);



}
