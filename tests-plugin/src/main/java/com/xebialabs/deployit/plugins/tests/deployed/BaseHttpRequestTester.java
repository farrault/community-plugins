/*
 * @(#)ExecutedSqlScript.java     1 Sep 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.plugins.tests.deployed;

import static com.google.common.base.Strings.nullToEmpty;

import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.Property.Size;
import com.xebialabs.deployit.plugin.generic.deployed.ExecutedScript;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugins.tests.step.LocalHttpTesterStep;
import com.xebialabs.deployit.plugins.tests.step.RemoteHttpTesterStep;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "Base class for the http request testers")
public class BaseHttpRequestTester extends ExecutedScript<Deployable> {

	@Property(description = "The URL to test")
	private String url;

	@Property(size = Size.LARGE, description = "Text that is expected to be contained in the HTTP response body, if the response code is in the 200 range. A non-2xx response code will cause the test to fail irrespective of the response body")
	private String expectedResponseText;

	@Property(description = "Show the page retrieved from the url")
	private boolean showPageInConsole;

	@Property(required = false, hidden = true, description = "Time in seconds to wait before starting the execution of step")
	private int startDelay = 0;

	@Property(required = false, hidden = true, description = "Number of times to attempt executing the step, incase it fails")
	private int noOfRetries = 0;

	@Property(required = false, hidden = true, description = "Time in seconds to wait before next retry")
	private int retryWaitInterval = 0;

	@Override
	public void executeCreate(DeploymentPlanningContext ctx, Delta d) {
		if (getContainer().getHost().getType().equals(Type.valueOf("overthere.LocalHost"))) {
			ctx.addStep(new LocalHttpTesterStep(getCreateOrder(), getDescription(getCreateVerb()), getContainer(), getUrl(), getExpectedResponseText(),
			        getStartDelay(), getNoOfRetries(), getRetryWaitInterval(), isShowPageInConsole()));
		} else {
			ctx.addStep(new RemoteHttpTesterStep(getCreateOrder(), getCreateScript(), getContainer(), getDeployedAsFreeMarkerContext(),
			        getDescription(getCreateVerb()), getStartDelay(), getNoOfRetries(), getRetryWaitInterval()));
		}
	}

	@Override
	public void executeDestroy(DeploymentPlanningContext ctx, Delta d) {
		// do nothing
	}

	public String getHostTemporaryDirectoryOrDefault() {
		Host host = getContainer().getHost();
		String hostTemporaryDirectory = nullToEmpty(host.getTemporaryDirectoryPath());
		return (!hostTemporaryDirectory.isEmpty() ? hostTemporaryDirectory : host.getOs().getDefaultTemporaryDirectoryPath());
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getExpectedResponseText() {
		return expectedResponseText;
	}

	public void setExpectedResponseText(String expectedResponseText) {
		this.expectedResponseText = expectedResponseText;
	}

	public boolean isShowPageInConsole() {
		return showPageInConsole;
	}

	public void setShowPageInConsole(boolean showPageInConsole) {
		this.showPageInConsole = showPageInConsole;
	}

	public int getStartDelay() {
		return startDelay;
	}

	public void setStartDelay(int startDelay) {
		this.startDelay = startDelay;
	}

	public int getNoOfRetries() {
		return noOfRetries;
	}

	public void setNoOfRetries(int noOfRetries) {
		this.noOfRetries = noOfRetries;
	}

	public int getRetryWaitInterval() {
		return retryWaitInterval;
	}

	public void setRetryWaitInterval(int retryWaitInterval) {
		this.retryWaitInterval = retryWaitInterval;
	}
}
