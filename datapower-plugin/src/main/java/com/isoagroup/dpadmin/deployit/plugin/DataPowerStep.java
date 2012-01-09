package com.isoagroup.dpadmin.deployit.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.StepExecutionContextCallbackHandler;
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;

@SuppressWarnings("serial")
public abstract class DataPowerStep implements Step {

	protected DataPowerAppliance appliance;
	protected String releaseName;
	protected String domainName;
	protected String flowName;
	protected String environmentId;

	public DataPowerStep(DataPowerAppliance appliance, String releaseName, String domainName, String flowName, String environmentId) {
		this.appliance = appliance;
		this.releaseName = releaseName;
		this.domainName = domainName;
		this.flowName = flowName;
		this.environmentId = environmentId;
	}

	protected boolean executeDpScript(HostSession s, String scriptResourceName, Map<String, String> params, StepExecutionContext ctx) {
		String dpautoPath = appliance.getDpadminHomePath() + "/bin/dpauto" + appliance.getDpadminHost().getOperatingSystemFamily().getScriptExtension();
		HostFile uploadedImportScript = s.copyToTemporaryFile(new ClassPathResource(scriptResourceName));

		List<String> dpautoCommand = new ArrayList<String>();
		dpautoCommand.add(dpautoPath);
		dpautoCommand.add(uploadedImportScript.getPath());
		dpautoCommand.add("releaseName:" + releaseName);
		dpautoCommand.add("domainName:" + domainName);
		dpautoCommand.add("flowName:" + flowName);
		dpautoCommand.add("environmentId:" + environmentId);
		for (Map.Entry<String, String> entry : params.entrySet()) {
			dpautoCommand.add(entry.getKey() + ":" + entry.getValue());
		}

		int res = s.execute(new StepExecutionContextCallbackHandler(ctx), dpautoCommand.toArray(new String[dpautoCommand.size()]));
		return res == 0;
	}

}
