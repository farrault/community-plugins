/*
 * Copyright (c) 2008-2011 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.deployit.plugin.apache.modjk.runbook;

import com.google.common.collect.Sets;
import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.ChangePlan;
import com.xebialabs.deployit.ChangeResolution;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.*;
import com.xebialabs.deployit.ci.artifact.StaticContent;
import com.xebialabs.deployit.ci.artifact.mapping.StaticContentMapping;
import com.xebialabs.deployit.plugin.apache.httpd.step.RestartApacheHttpdServerStep;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfigurationMapping;
import com.xebialabs.deployit.plugin.apache.modjk.step.CreateModkJKConfigFileStep;
import com.xebialabs.deployit.plugin.tomcat.ci.TomcatServer;
import com.xebialabs.deployit.plugin.tomcat.ci.TomcatUnmanagedServer;
import com.xebialabs.deployit.steps.CopyStep;
import com.xebialabs.deployit.test.support.stubs.StubChange;
import com.xebialabs.deployit.test.support.stubs.StubChangePlan;
import com.xebialabs.deployit.test.support.utils.DebugStepExecutionContext;
import com.xebialabs.deployit.test.support.utils.RunBookTestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DeployStaticContentWarWithModJKTest extends BaseTestForModJKRunBook {

	protected Deployment deployment;
	private ModJkApacheModuleConfigurationMapping jkmapping;

	private StaticContentMapping staticMapping;

	@Before
	public void setup() throws IOException {

		TomcatServer as1 = new TomcatUnmanagedServer();
		as1.setLabel("Tomcatas1");
		as1.setAjpPort(8009);
		as1.setHost(host1);

		TomcatServer as2 = new TomcatUnmanagedServer();
		as2.setLabel("Tomcatas2");
		as2.setAjpPort(8010);
		as2.setHost(host2);


		Set<LoadBalancedServerAware> ass = Sets.newHashSet();
		ass.add(as1);
		ass.add(as2);

		Environment e = new Environment();
		e.addMember(host1);
		e.addMember(apacheHttpdServer);
		e.addMember(as1);
		e.addMember(as2);

		File htmlContents = File.createTempFile("html1", ".dir");
		htmlContents.delete();
		htmlContents.mkdir();
		htmlContents.deleteOnExit();
		StaticContent html1 = new StaticContent();
		html1.setLabel("HTML files and images");
		html1.setLocation(htmlContents.getPath());

		DeploymentPackage dp = new DeploymentPackage();
		dp.setLabel("DP-TEST");
		dp.setApplication(new Application());
		dp.addMiddlewareResource(jkConfiguration);
		dp.addDeployableArtifact(html1);


		jkmapping = new ModJkApacheModuleConfigurationMapping();
		jkmapping.setLabel("Mapping Of MyApplication");
		jkmapping.setSource(jkConfiguration);
		jkmapping.setTarget(apacheHttpdServer);
		jkmapping.setVirtualHost("my.pet.local:8890");
		jkmapping.setTargets(ass);

		staticMapping = new StaticContentMapping();
		staticMapping.setLabel("a Static Content Mapping ");
		staticMapping.setVirtualHost("my.pet.local:8890");
		staticMapping.setSource(html1);
		staticMapping.setTarget(apacheHttpdServer);

		deployment = new Deployment();
		deployment.setSource(dp);
		deployment.setTarget(e);
		deployment.addMapping(jkmapping);
		deployment.addMapping(staticMapping);
	}

	@Test
	public void testDeployit() throws Exception {
		Change<Deployment> deploymentChange = new StubChange<Deployment>(null, deployment);
		ChangePlan cp = new StubChangePlan(deploymentChange);
		Collection<ChangeResolution> crs = runbook.resolve(cp);
		List<Step> steps = RunBookTestUtils.assertOneResolutionAndGetItsSteps(crs);
		RunBookTestUtils.assertTypeSequence(steps, CopyStep.class, CreateModkJKConfigFileStep.class, RestartApacheHttpdServerStep.class);
		CopyStep cpStep = RunBookTestUtils.getStepsOfClass(steps, CopyStep.class).iterator().next();
		System.out.println(cpStep.getDescription());
		CreateModkJKConfigFileStep step = RunBookTestUtils.getStepsOfClass(steps, CreateModkJKConfigFileStep.class).iterator().next();
		step.execute(new DebugStepExecutionContext());

	}
}