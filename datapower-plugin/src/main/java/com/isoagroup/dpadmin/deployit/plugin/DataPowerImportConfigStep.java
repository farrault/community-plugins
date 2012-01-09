package com.isoagroup.dpadmin.deployit.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.hostsession.HostSessionFactory;

@SuppressWarnings("serial")
public class DataPowerImportConfigStep extends DataPowerStep {

	protected DataPowerConfig export;

	public DataPowerImportConfigStep(DataPowerConfigToApplianceMapping mapping) {
		super(mapping.getTarget(), "release-deployit", mapping.getSource().getDomainName(), mapping.getFlowName(), mapping.getTarget().getDeviceAlias());
		export = mapping.getSource();
	}

	public String getDescription() {
		return "Import exported DataPower configuration " + export + " on appliance " + appliance + " using flow name " + flowName;
	}

	public boolean execute(StepExecutionContext ctx) {
		HostSession s = HostSessionFactory.getHostSession(appliance.getDpadminHost());
		try {
			HostFile uploadedExportFile = s.copyToTemporaryFile(new File(export.getLocation()));
			Map<String, String> params = new HashMap<String, String>();

			params.put("export", uploadedExportFile.getPath());
			params.put("snapshotName", "snapshot-" + System.currentTimeMillis());
			params.put("xmlManagerName", export.getXmlManagerName());

			return executeDpScript(s, "com/isoagroup/dpadmin/deployit/plugin/ImportConfig.xml", params, ctx);
		} finally {
			s.close();
		}
	}

}
