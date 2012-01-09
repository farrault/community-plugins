package com.isoagroup.dpadmin.deployit.plugin;

import com.xebialabs.deployit.StepExecutionContext;

public class DebugStepExecutionContext implements StepExecutionContext {

	@Override
	public Object getAttribute(String arg0) {
		return null;
	}

	@Override
	public void logError(String arg0) {
		System.err.println(arg0);

	}

	@Override
	public void logError(String arg0, Throwable arg1) {
		System.err.println(arg0);
		arg1.printStackTrace();
	}

	@Override
	public void logOutput(String arg0) {
		System.out.println(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {

	}

}
