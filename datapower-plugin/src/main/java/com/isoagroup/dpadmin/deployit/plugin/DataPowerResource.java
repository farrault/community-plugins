package com.isoagroup.dpadmin.deployit.plugin;

import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.artifact.NamedDeployableArtifact;

@SuppressWarnings("serial")
@ConfigurationItem
public class DataPowerResource extends NamedDeployableArtifact {

	// FIXME: Set required=true when importer problem has been solved
	@ConfigurationItemProperty(required = false)
	private String domainName;

	// FIXME: Set required=true when importer problem has been solved
	@ConfigurationItemProperty(required = false)
	private String xmlManagerName;

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getXmlManagerName() {
		return xmlManagerName;
	}

	public void setXmlManagerName(String xmlManagerName) {
		this.xmlManagerName = xmlManagerName;
	}

}
