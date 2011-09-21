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

import com.google.common.collect.Lists;
import com.xebialabs.deployit.BaseConfigurationItem;
import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.MiddlewareResource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;

import java.util.Collections;
import java.util.List;

/**
 * Configuration for the mod_jk module for Apache in situations where the target environment contains
 * Apache web servers with a ModJK Configuration which &quot;front&quot; Tomcat or JBoss servers.
 */
@SuppressWarnings("serial")
@ConfigurationItem(description = "An abstraction of Apache modk_jk Configuration. It is used to generate the configuration file which is included in the Apache main httpd.conf file")
public class ModJkApacheModuleConfiguration extends BaseConfigurationItem implements MiddlewareResource {

	@ConfigurationItemProperty(required = true, identifying = true, description = "used to identify the modjk configuration")
	private String configurationName;

	@ConfigurationItemProperty(description = "Comma separated list of  mounted URL in the mod_jk")
	private String urlMounts;

	@ConfigurationItemProperty(description = "Comma separated list of unmounted URL  in the mod_jk")
	private String urlUnmounts;

	@ConfigurationItemProperty(description = "handle jkStatus configuration, default false")
	private boolean jkstatus = false;

	@ConfigurationItemProperty(description = "handle jkStatus log level, eg Error, default is Info")
	private LogLevel jkLogLevel = LogLevel.Info;

	@ConfigurationItemProperty(description = "Configure the date/time format found on mod_jk logfile, default is [%a %b %d %H:%M:%S %Y]")
	private String jkLogStampFormat = "[%a %b %d %H:%M:%S %Y]";

	@ConfigurationItemProperty(description = "Allow you to set many forwarding options which will enable (+) or disable (-) following option")
	private String JkOptions;

	@ConfigurationItemProperty(description = "Cachesize defines the number of connections made to the AJP backend that are maintained as a connection pool. It will limit the number of those connection that each web server child process can make.")
	private int cacheSize;

	@ConfigurationItemProperty(description = "Cache timeout property should be used with cachesize to specify how to time JK should keep an open socket in cache before closing it. This property should be used to reduce the number of threads on the Tomcat web server.")
	private int cacheTimeout;

	@ConfigurationItemProperty(description = "This directive should be used when you have a firewall between your webserver and the Tomcat engine, who tend to drop inactive connections.")
	private boolean socketKeepAlive;

	@ConfigurationItemProperty(description = "Socket timeout in seconds used for the communication channel between JK and remote host, default 0")
	private int socketTimeout = 0;

	@ConfigurationItemProperty(description = "Specifies whether requests with SESSION ID's should be routed back to the same Tomcat worker, default True")
	private boolean stickySession = true;

	public List<String> getUrlMounts() {
		return splitToURLPrefix(urlMounts);
	}

	public void setUrlMounts(String urlMounts) {
		this.urlMounts = urlMounts;
	}

	public void setUrlUnmounts(String urlUnmounts) {
		this.urlUnmounts = urlUnmounts;
	}

	public List<String> getUrlUnmounts() {
		return splitToURLPrefix(urlUnmounts);
	}

	public boolean isJkstatus() {
		return jkstatus;
	}

	public void setJkstatus(boolean jkstatus) {
		this.jkstatus = jkstatus;
	}

	public LogLevel getJkLogLevel() {
		return jkLogLevel;
	}

	public void setJkLogLevel(LogLevel jkLogLevel) {
		this.jkLogLevel = jkLogLevel;
	}

	public String getJkLogStampFormat() {
		return jkLogStampFormat;
	}

	public void setJkLogStampFormat(String jkLogStampFormat) {
		this.jkLogStampFormat = jkLogStampFormat;
	}

	public String getJkOptions() {
		return JkOptions;
	}

	public void setJkOptions(String jkOptions) {
		JkOptions = jkOptions;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public int getCacheTimeout() {
		return cacheTimeout;
	}

	public void setCacheTimeout(int cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}

	public boolean isSocketKeepAlive() {
		return socketKeepAlive;
	}

	public void setSocketKeepAlive(boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public boolean isStickySession() {
		return stickySession;
	}

	public void setStickySession(boolean stickySession) {
		this.stickySession = stickySession;
	}

	private List<String> splitToURLPrefix(String commaseparatedlist) {
		if (StringUtils.isEmpty(commaseparatedlist))
			return Collections.emptyList();
		return Lists.newArrayList(StrTokenizer.getCSVInstance(commaseparatedlist).getTokenArray());
	}

}
