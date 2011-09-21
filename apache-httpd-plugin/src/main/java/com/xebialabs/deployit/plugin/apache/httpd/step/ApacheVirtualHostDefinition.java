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

import static com.xebialabs.deployit.util.ResolutionUtils.checkNotEmpty;
import static com.xebialabs.deployit.util.ResolutionUtils.checkValidHostAndPort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

import com.xebialabs.deployit.ResolutionException;
import com.xebialabs.deployit.ci.HttpdServer.HostAndPort;
import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.deployit.util.ExtendedResourceUtils;
import com.xebialabs.deployit.util.TemplateResolver;

/**
 * Creates a String representation of a VirtualHost definition for Apache Httpd and
 * computes its filename. For example, the virtual host definition {@code www.xebialabs.com:443}
 * would result in
 * 
 * <ul>
 * <li>NameVirtualHost: {@code *:443}
 * <li>DocumentRoot: <code><em>htdocsLocation</em>/www.xebialabs.com_443</code> 
 * <li>ServerName: {@code www.xebialabs.com}
 * </ul>
 * 
 */
public class ApacheVirtualHostDefinition {
	private static String DEFAULT_APACHE_VHOST_TEMPLATE = "com/xebialabs/deployit/plugin/apache/httpd/step/apache_httpd_server_vhost_template.conf";

	private final Collection<? extends Object> existingContext;

	private String documentRoot;
	private String host;
	private String port;
	private String nameVirtualHost;
	private String fileName;
	private String vhostDefinition;

	/**
	 * Constructs a VirtualHostDefinition based on a single vhostDefinition.
	 * 
	 * @param vhostDefinition
	 */
	public ApacheVirtualHostDefinition(String vhostDefinition, String htdocsLocation, OperatingSystemFamily osf, Collection<?> existingContext)
			throws ResolutionException {
		this.existingContext = existingContext;
		checkNotEmpty(vhostDefinition, "No vhost definition provided");
		checkNotEmpty(htdocsLocation, "No htdocs provided");

		HostAndPort vhostAndPort = checkValidHostAndPort(vhostDefinition);
		host = vhostAndPort.getHost();
		port = vhostAndPort.getPort();

		nameVirtualHost = "*:" + port;

		final String fileNameFriendlyVhost = vhostAndPort.getFilenameFriendlyHost() 
			+ "_" + vhostAndPort.getFilenameFriendlyPort();

		fileName = fileNameFriendlyVhost + ".conf";
		documentRoot = htdocsLocation + osf.getFileSeparator() + fileNameFriendlyVhost;

		if (existingContext != null) {
			this.vhostDefinition = resolveVhostDefinition();
		}
	}

	protected String resolveVhostDefinition() {
		String template;
		List<Object> context = new ArrayList<Object>();
		context.addAll(existingContext);
		context.add(this);
		template = ExtendedResourceUtils.toString(new ClassPathResource(DEFAULT_APACHE_VHOST_TEMPLATE));
		TemplateResolver r = new TemplateResolver(context);
		return r.resolveStrict(template);
	}

	public String toVirtualHostDefinition() {
		return vhostDefinition;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	public String getDocumentRoot() {
		return documentRoot;
	}

	public void setDocumentRoot(String documentRoot) {
		this.documentRoot = documentRoot;
	}

	public String getNameVirtualHost() {
		return nameVirtualHost;
	}

	public void setNameVirtualHost(String nameVirtualHost) {
		this.nameVirtualHost = nameVirtualHost;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
