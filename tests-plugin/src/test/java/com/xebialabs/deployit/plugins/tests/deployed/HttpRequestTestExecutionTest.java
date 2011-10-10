/*
 * @(#)TestBase.java     3 Sep 2011
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

import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xebialabs.deployit.deployment.planner.DeltaSpecificationBuilder;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.execution.Plan;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeploymentPackage;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.ci.Resource;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugins.tests.TestBase;
import com.xebialabs.deployit.test.deployment.DeployitTester;
import com.xebialabs.overthere.OperatingSystemFamily;

/**
 * Unit tests for the {@link HttpRequestTestExecution}
 */
public class HttpRequestTestExecutionTest extends TestBase {
	Container container;

	@BeforeClass
	public static void boot() {
		PluginBooter.bootWithoutGlobalContext();
		tester = DeployitTester.build();
	}

	protected static DeployitTester tester;

	@Before
	public void setup() {
		Host host = newInstance("overthere.CifsHost");
		host.setId("Infrastructure/overthere");
		host.setOs(OperatingSystemFamily.WINDOWS);
		host.putSyntheticProperty(CONNECTION_TYPE, TELNET);
		host.putSyntheticProperty(ADDRESS, "overthere");
		host.putSyntheticProperty(USERNAME, "overthere");
		host.putSyntheticProperty(PASSWORD, "overhere");
		
		container = newInstance("tests.TestStation");
		container.setId("Infrastructure/testContainer");
		container.setHost(host);
		
		environment = createEnvironment(container);
	}

	@Test
	public void testerAddStepForTesting() {
		Resource testerSpec = newInstance("test.HttpRequestTesterSpec");
		testerSpec.putSyntheticProperty("url", "http://localhost:8080/petclinic");
		testerSpec.putSyntheticProperty("expectedResponseText", "petclinic");

		DeploymentPackage package1_0 = createDeploymentPackage("1.0", testerSpec);

		Deployed<Deployable, Container> httpTester = newInstance("test.HttpRequestTester");
		httpTester.setContainer(container);
		httpTester.setDeployable(testerSpec);
		
		DeltaSpecification spec = new DeltaSpecificationBuilder().initial(package1_0, environment).create(httpTester).build();
		Plan resolvedPlan = tester.resolvePlan(spec);
		List<DeploymentStep> resolvedSteps = resolvedPlan.getSteps();

		assertThat(resolvedSteps.size(), is(1));
	}
}
