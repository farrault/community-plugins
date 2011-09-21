package com.xebialabs.deployit.plugin.cloudburst;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.StepExecutionContextCallbackHandler;
import com.xebialabs.deployit.hostsession.CommandExecutionCallbackHandler;
import com.xebialabs.deployit.hostsession.HostFile;
import com.xebialabs.deployit.hostsession.HostSession;
import com.xebialabs.deployit.hostsession.HostSessionFactory;

@SuppressWarnings("serial")
public abstract class CloudBurstCliStep implements Step {

	private final String description;
	private final CloudBurstAppliance appliance;
	private final String scriptResource;
	private final String[] args;

	public CloudBurstCliStep(final String description, final CloudBurstAppliance appliance, final String scriptResource, final String... args) {
		this.description = description;
		this.appliance = appliance;
		this.scriptResource = scriptResource;
		this.args = args;
	}

	protected int execute(CommandExecutionCallbackHandler handler) {
		HostSession s = HostSessionFactory.getHostSession(appliance.getCliHost());
		try {
			HostFile scriptFileOnCliHost = s.copyToTemporaryFile(new ClassPathResource(scriptResource));
			List<String> cmdline = new ArrayList<String>();
			cmdline.add(appliance.getCliHome() + "/bin/cloudburst");
			cmdline.add("-h");
			cmdline.add(appliance.getAddress());
			cmdline.add("-u");
			cmdline.add(appliance.getUsername());
			cmdline.add("-p");
			cmdline.add(appliance.getPassword());
			cmdline.add("-f");
			cmdline.add(scriptFileOnCliHost.getPath());
			for (String each : args) {
				cmdline.add(each);
			}
			String[] cmdlineArray = cmdline.toArray(new String[cmdline.size()]);
			return s.execute(handler, cmdlineArray);
		} finally {
			s.close();
		}
	}

	@Override
	public boolean execute(StepExecutionContext context) {
		int res = execute(new StepExecutionContextCallbackHandler(context));
		return res == 0;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
