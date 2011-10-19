package com.xebialabs.deployit.plugins.tests.step;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.slf4j.MDC;

import com.xebialabs.deployit.plugin.generic.freemarker.FileUploader;
import com.xebialabs.deployit.plugin.generic.step.ScriptExecutionStep;
import com.xebialabs.deployit.plugin.overthere.ExecutionContextOverthereProcessOutputHandler;
import com.xebialabs.deployit.plugin.overthere.HostContainer;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

@SuppressWarnings("serial")
public class RemoteHttpTesterStep extends ScriptExecutionStep {
	private static final String MDC_KEY_SCRIPT_PATH = "scriptPath";

	private String scriptTemplatePath;
	private Map<String, Object> vars;

	private int startDelay;
	private int noOfRetries;
	private int retryWaitInterval;

	public RemoteHttpTesterStep(int order, String scriptPath, HostContainer hostContainer, Map<String, Object> vars, String description, int startDeplay,
	        int noOfRetries, int retryWaitInterval) {
		super(order, scriptPath, hostContainer, vars, description);
		this.scriptTemplatePath = scriptPath;
		this.vars = vars;

		this.startDelay = startDeplay;
		this.noOfRetries = noOfRetries;
		this.retryWaitInterval = retryWaitInterval;
	}

	/**
	 * ...wish the actual execute (getRemoteConnection().execute(handle, cmdLine)) call was in a separate method which I simply could override..
	 */

	@Override
	public Result doExecute() throws Exception {
		MDC.put(MDC_KEY_SCRIPT_PATH, scriptTemplatePath);
		try {
			String osSpecificTemplate = resolveOsSpecificTemplate();
			String executableContent = evaluateTemplate(osSpecificTemplate, vars);
			logger.debug(executableContent);
			OverthereFile executable = uploadExecutable(executableContent, substringAfterLast(osSpecificTemplate, '/'));
			return executeScript(executable);
		} finally {
			MDC.remove(MDC_KEY_SCRIPT_PATH);
		}
	}

	protected Result executeScript(OverthereFile executable) {
		CmdLine cmdLine = CmdLine.build(executable.getPath());
		OverthereProcessOutputHandler handle = new ExecutionContextOverthereProcessOutputHandler(getCtx());
		getCtx().logOutput("Waiting for " + startDelay + " secs before executing step");
		waitFor(startDelay);
		int rc = execute(executable, cmdLine, handle);
		if (rc != 0) {
			for (int i = 0; i < noOfRetries; i++) {
				getCtx().logOutput("\n");
				getCtx().logOutput("Attempting retry " + (i + 1) + " of " + noOfRetries + " in " + retryWaitInterval + " seconds");
				waitFor(retryWaitInterval);

				rc = execute(executable, cmdLine, handle);

				if (rc == 0) {
					break;
				}
			}
		}
		return rc == 0 ? Result.Success : Result.Fail;
	}

	private int execute(OverthereFile executable, CmdLine cmdLine, OverthereProcessOutputHandler handle) {
		getCtx().logOutput("Executing " + executable.getPath() + " on host " + getContainer().getHost());
		int rc = getRemoteConnection().execute(handle, cmdLine);
		if (rc != 0) {
			getCtx().logError("Execution failed with return code " + rc);
		}
		return rc;
	}

	private void waitFor(int seconds) {
		try {
			if (seconds > 0)
				Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private OverthereFile uploadExecutable(String content, String fileName) {
		OverthereFile targetExecutable = getRemoteWorkingDirectory().getFile(fileName);
		OverthereUtils.write(content.getBytes(), targetExecutable);
		targetExecutable.setExecutable(true);
		return targetExecutable;
	}

	private String resolveOsSpecificTemplate() {
		String osSpecificScript = scriptTemplatePath;

		String scriptExt = substringAfterLast(scriptTemplatePath, '.');
		if (scriptExt == null) {
			OperatingSystemFamily os = getContainer().getHost().getOs();
			osSpecificScript = osSpecificScript + os.getScriptExtension();
		}

		if (!classpathResourceExists(osSpecificScript)) {
			throw new IllegalArgumentException("Resource " + osSpecificScript + " not found in classpath");
		}

		return osSpecificScript;
	}

	private class WorkingFolderUploader implements FileUploader {
		private Map<String, String> uploadedFiles = newHashMap();

		@Override
		public String upload(OverthereFile file) {
			if (uploadedFiles.containsKey(file.getName())) {
				return uploadedFiles.get(file.getName());
			}
			OverthereFile uploadedFile = getRemoteWorkingDirectory().getFile(file.getName());
			file.copyTo(uploadedFile);
			uploadedFiles.put(file.getName(), uploadedFile.getPath());
			return uploadedFile.getPath();
		}
	}
}
