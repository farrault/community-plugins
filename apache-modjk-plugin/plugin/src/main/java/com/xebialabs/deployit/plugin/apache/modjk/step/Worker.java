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

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Properties;

public class Worker implements Serializable {

	private static final String AJP_13 = "ajp13";

	private String name;
	private String address;
	private int port;
	private int lbFactor = 1;
	private int cacheSize;
	private int cacheTimeout;
	private boolean socketKeepAlive = false;
	private int socketTimeout;

	private String type = AJP_13;

	public Worker() {

	}

	public Worker(String name, String address, int port) {
		Preconditions.checkNotNull(name, "Worker name is null");
		this.name = convert(name);
		this.address = address;
		this.port = port;
	}


	public Worker(String name, Properties properties) {
		this.name = name;

		this.address = getStringProperty("host", properties, "0.0.0.0");
		this.type = getStringProperty("type", properties, AJP_13);
		this.port = Integer.parseInt(properties.getProperty("worker." + name + ".port", "8009"));

		this.cacheSize = getIntProperty("cachesize", properties);
		this.cacheTimeout = getIntProperty("cache_timeout", properties);

		this.socketKeepAlive = getIntProperty("socket_keepalive", properties) > 0;
		this.socketTimeout = getIntProperty("socket_timeout", properties);
	}

	private String getStringProperty(String attributeName, Properties properties, String defaultValue) {
		return properties.getProperty("worker." + name + "." + attributeName, defaultValue);
	}

	private int getIntProperty(String attributeName, Properties properties) {
		return Integer.parseInt(properties.getProperty("worker." + name + "." + attributeName, "0"));
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setType(String type) {
		this.type = type;
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

	public int getLbFactor() {
		return lbFactor;
	}

	public void setLbFactor(int lbFactor) {
		this.lbFactor = lbFactor;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Worker)) return false;

		Worker worker = (Worker) o;

		if (!name.equals(worker.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	private String convert(String s) {
		return s.replace(' ', '.');
	}

}
