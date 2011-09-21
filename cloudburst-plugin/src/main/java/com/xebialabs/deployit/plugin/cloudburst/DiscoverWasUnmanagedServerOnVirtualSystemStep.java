package com.xebialabs.deployit.plugin.cloudburst;

import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.deployit.ci.HostAccessMethod.SSH_SFTP;
import static com.xebialabs.deployit.ci.OperatingSystemFamily.UNIX;

import java.util.Map;

import com.xebialabs.deployit.ChangePlan;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.StepExecutionContext;
import com.xebialabs.deployit.ci.Host;
import com.xebialabs.deployit.plugin.was.ci.WasUnmanagedServer;
import com.xebialabs.deployit.task.TaskExecutionContext;
import com.xebialabs.deployit.translation.DiscoveryChangePlan;

@SuppressWarnings("serial")
public class DiscoverWasUnmanagedServerOnVirtualSystemStep implements Step {

	public static final String WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX = "virtualsystem.wasunmanagedserver.";

	private String systemName;

	private String systemPassword;

	public DiscoverWasUnmanagedServerOnVirtualSystemStep(String systemName, String systemPassword) {
		this.systemName = systemName;
		this.systemPassword = systemPassword;
	}

	@Override
	public String getDescription() {
		return "Discover WebSphere Application Server on virtual system " + systemName;
	}

	@Override
	public boolean execute(StepExecutionContext ctx) {
		String hostname = (String) ctx.getAttribute(ReadVirtualSystemStep.VIRTUAL_SYSTEM_HOST_NAME_PREFIX + systemName);
		if (hostname == null) {
			throw new IllegalStateException("Cannot find hostname for virtual system " + systemName + " in step execution context");
		}

		Host virtualSystemHost = new Host();
		virtualSystemHost.setLabel(hostname);
		virtualSystemHost.setAddress(hostname);
		virtualSystemHost.setUsername("root");
		virtualSystemHost.setPassword(systemPassword);
		virtualSystemHost.setOperatingSystemFamily(UNIX);
		virtualSystemHost.setAccessMethod(SSH_SFTP);
		WasUnmanagedServer server1 = new WasUnmanagedServer();
		server1.setHost(virtualSystemHost);
		server1.setLabel(hostname + "/server1");
		server1.setUsername("virtuser");
		server1.setPassword(systemPassword);
		server1.setWasHome("/opt/IBM/WebSphere/Profiles/DefaultAppSrv01");
		server1.setPort(8880);

		// Need nicer way to trigger discovery from plugin API itself!
		ChangePlan cp = new DiscoveryChangePlan();
		WasUnmanagedServer discoveredServer1;
		Map<String, Object> info = newHashMap();
		@SuppressWarnings("deprecation")
        TaskExecutionContext context = new TaskExecutionContext(info);
		try {
			discoveredServer1 = server1.discover(info, cp);
		} finally {
			context.destroy();
		}

		ctx.setAttribute(WAS_UNMANAGED_SERVER_ON_VIRTUAL_SYSTEM_PREFIX + systemName, discoveredServer1);

		return true;
	}

}
