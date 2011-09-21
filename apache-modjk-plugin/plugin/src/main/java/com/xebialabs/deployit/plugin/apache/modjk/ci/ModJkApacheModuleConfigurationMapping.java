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

package com.xebialabs.deployit.plugin.apache.modjk.ci;

import com.google.common.collect.Sets;
import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ResolutionException;
import com.xebialabs.deployit.ci.ListenServer;
import com.xebialabs.deployit.ci.LoadBalancedServerAware;
import com.xebialabs.deployit.ci.LoadBalancingProtocol;
import com.xebialabs.deployit.ci.mapping.SourcePropertyOverridingMapping;
import com.xebialabs.deployit.plugin.apache.httpd.ci.ApacheHttpdServer;
import com.xebialabs.deployit.plugin.apache.modjk.step.LoadBalancer;
import com.xebialabs.deployit.plugin.apache.modjk.step.Worker;

import java.util.List;
import java.util.Set;


@ConfigurationItem
public class ModJkApacheModuleConfigurationMapping extends SourcePropertyOverridingMapping<ModJkApacheModuleConfiguration, ApacheHttpdServer> {

	@ConfigurationItemProperty(required = true, description = "the virtual host associated with the modjk configuration")
	private String virtualHost;

	@ConfigurationItemProperty(description = "Set of the targets - Tomcat or JBoss- implied in the load balancing")
	private Set<LoadBalancedServerAware> targets = Sets.newHashSet();

	@ConfigurationItemProperty(description = "Prefix used to build the name of the worker, default LB-")
	private String loadBalancerPrefixName = "LB-";


	public Set<Worker> getWorkers() {
		if (targets.isEmpty())
			throw new ResolutionException("Select at least one target");

		Set<Worker> workers = Sets.newHashSet();
		for (LoadBalancedServerAware target : targets) {
			final List<LoadBalancingProtocol> protocolList = target.getSupportedProtocols();
			if (protocolList.contains(LoadBalancingProtocol.AJP)) {
				final List<ListenServer> serverList = target.getLoadBalancedServers(LoadBalancingProtocol.AJP);
				for (ListenServer server : serverList)
					workers.add(update(new Worker(server.getLabel(), server.getHost().getAddress(), server.getListenPort())));
			}
		}
		return workers;
	}

	public String getLoadBalancerName() {
		return loadBalancerPrefixName + getVirtualHost().replace(':', '_').replace('*', '_');
	}

	public LoadBalancer getLoadBalancer() {
		LoadBalancer lb = new LoadBalancer(getLoadBalancerName(), getWorkers());
		lb.setStickySession(getSource().isStickySession() ? 1 : 0);
		return lb;
	}

	protected Worker update(Worker worker) {
		final ModJkApacheModuleConfiguration configuration = getSource();
		worker.setCacheSize(configuration.getCacheSize());
		worker.setCacheTimeout(configuration.getCacheTimeout());
		worker.setSocketKeepAlive(configuration.isSocketKeepAlive());
		worker.setSocketTimeout(configuration.getSocketTimeout());
		return worker;
	}


	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}


	public Set<LoadBalancedServerAware> getTargets() {
		return targets;
	}

	public void setTargets(Set<LoadBalancedServerAware> targets) {
		this.targets = targets;
	}
}
