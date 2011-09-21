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


import com.xebialabs.deployit.util.ExtendedStringUtils;
import org.apache.commons.collections.Transformer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class LoadBalancer implements Serializable {

	private final String name;
	private final Set<Worker> workers = new HashSet<Worker>();
	private final Set<String> workerNames = new HashSet<String>();
	private int stickySession = 1;

	public LoadBalancer(String name, Set<Worker> workers) {
		this.name = name;
		this.workers.addAll(workers);
	}

	public LoadBalancer(String name, Properties properties) {
		this.name = name;
		//String bw = getProperty("balance_workers", properties);
		String bw = properties.getProperty("worker." + name + "." + "balance_workers", "");
		for (String w : bw.split(","))
			workerNames.add(w);

		stickySession = Integer.parseInt(properties.getProperty("worker." + name + ".sticky_session", "1"));
	}

	public void addWorker(Worker w) {
		if (workerNames.contains(w.getName()))
			workers.add(w);
	}

	public Set<Worker> getWorkers() {
		return Collections.unmodifiableSet(workers);
	}

	public int getStickySession() {
		return stickySession;
	}

	public void setStickySession(int stickySession) {
		this.stickySession = stickySession;
	}

	public String getWorkerNames() {
		return ExtendedStringUtils.join(workers, new Transformer() {
			public String transform(Object obj) {
				Worker worker = (Worker) obj;
				if (worker == null)
					return "XXX";
				return worker.getName();
			}
		});

	}

	public String getName() {
		return name;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LoadBalancer)) return false;

		LoadBalancer that = (LoadBalancer) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}
