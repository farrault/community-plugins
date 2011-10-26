package com.xebialabs.deployit.plugins.tests.step;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentExecutionContext;
import com.xebialabs.deployit.plugin.generic.step.GenericBaseStep;
import com.xebialabs.deployit.plugin.overthere.HostContainer;

@SuppressWarnings("serial")
public class LocalHttpTesterStep extends GenericBaseStep {
	private String url;
	private String expectedResponseText;
	private boolean showPageInConsole;
	private int startDelay;
	private int noOfRetries;
	private int retryWaitInterval;

	public LocalHttpTesterStep(int order, String description, HostContainer hostContainer, String url, String expectedResponseText, int startDeplay, int noOfRetries,
	        int retryWaitInterval, boolean showPageInConsole) {
		super(order, description, hostContainer);
		this.url = url;
		this.expectedResponseText = expectedResponseText;
		this.startDelay = startDeplay;
		this.noOfRetries = noOfRetries;
		this.retryWaitInterval = retryWaitInterval;
		this.showPageInConsole = showPageInConsole;
	}

	@Override
	protected Result doExecute() throws Exception {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(url);
		try {
			getCtx().logOutput("Waiting for " + startDelay + " secs before executing step");
			waitFor(startDelay);
			return verifyContentWithRetryAttempts(getCtx(), client, method);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			method.releaseConnection();
		}

		return Result.Fail;
	}

	private Result verifyContentWithRetryAttempts(DeploymentExecutionContext ctx, HttpClient client, GetMethod method) throws InterruptedException {
		for (int i = 0; i < noOfRetries; i++) {
			try {
				int statusCode = client.executeMethod(method);
				if (statusCode == HttpStatus.SC_OK) {
					return verifyContent(ctx, method);
				} else {
					ctx.logError("Failed to access url. Status code " + statusCode);
				}
			} catch (IOException e) {
				ctx.logError("Failed to access url " + url + ". Error " + e.getClass().getName() + ", msg: " + e.getMessage());
			}
			ctx.logOutput("Attempting retry " + (i + 1) + " of " + noOfRetries + " in " + retryWaitInterval + " seconds");
			waitFor(retryWaitInterval);
		}

		return Result.Fail;
	}

	private Result verifyContent(DeploymentExecutionContext ctx, GetMethod method) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
		Pattern pattern = Pattern.compile(expectedResponseText);
		String line;
		Result result = Result.Fail;
		while ((line = reader.readLine()) != null) {
			if (showPageInConsole) {
				ctx.logOutput(line);
			}
			if (result == Result.Fail)
				result = matchLine(ctx, pattern, line);
		}

		if (result == Result.Fail)
			ctx.logError("Failed to find content '" + expectedResponseText + "' in page.");

		return Result.Success;
	}

	private Result matchLine(DeploymentExecutionContext ctx, Pattern pattern, String line) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			ctx.logOutput("Successfully found content '" + expectedResponseText + "' in page.");
			return Result.Success;
		}
		return Result.Fail;
	}

	private void waitFor(int seconds) throws InterruptedException {
		if (seconds > 0)
			Thread.sleep(seconds * 1000);
	}

}
