package com.isoagroup.dpadmin.deployit.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.hostsession.HostSessionFactory;

@SuppressWarnings("serial")
public class DataPowerImportResourceStep extends DataPowerStep {

	protected DataPowerResource resource;
	
	protected String targetDirectory;

	public DataPowerImportResourceStep(DataPowerResourceToApplianceMapping mapping) {
		super(mapping.getTarget(), "release-deployit", mapping.getSource().getDomainName(), mapping.getFlowName(), mapping.getTarget().getDeviceAlias());
		resource = mapping.getSource();
		targetDirectory = mapping.getTargetDirectory();
	}

	public String getDescription() {
		return "Import DataPower resource " + resource + " on appliance " + appliance + " using flow name " + flowName;
	}

	public boolean execute(StepExecutionContext ctx) {
		HostSession s = HostSessionFactory.getHostSession(appliance.getDpadminHost());
		try {
			File resourceFile = new File(resource.getLocation());
			HostFile uploadedResourceFile = s.copyToTemporaryFile(resourceFile);

			Map<String, String> params = new HashMap<String, String>();
			params.put("resource", uploadedResourceFile.getPath());
			params.put("targetDirectory", targetDirectory);
			params.put("targetFilename", targetDirectory + "/" + resource.getName());
			params.put("xmlManagerName", resource.getXmlManagerName());

			return executeDpScript(s, "com/isoagroup/dpadmin/deployit/plugin/ImportResource.xml", params, ctx);
		} finally {
			s.close();
		}
	}

}
