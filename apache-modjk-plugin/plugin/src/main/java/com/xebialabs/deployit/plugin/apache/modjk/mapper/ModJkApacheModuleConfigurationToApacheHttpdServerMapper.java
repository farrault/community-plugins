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

package com.xebialabs.deployit.plugin.apache.modjk.mapper;

import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.Deployment;
import com.xebialabs.deployit.mapper.StepGeneratingMapper;
import com.xebialabs.deployit.plugin.apache.httpd.ci.ApacheHttpdServer;
import com.xebialabs.deployit.plugin.apache.httpd.step.RestartApacheHttpdServerStep;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfiguration;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfigurationMapping;
import com.xebialabs.deployit.plugin.apache.modjk.step.CreateModkJKConfigFileStep;
import com.xebialabs.deployit.plugin.apache.modjk.step.DeleteModkJKConfigFileStep;

import java.util.List;

public class ModJkApacheModuleConfigurationToApacheHttpdServerMapper
		extends StepGeneratingMapper<ModJkApacheModuleConfiguration, ModJkApacheModuleConfigurationMapping, ApacheHttpdServer> {

	public ModJkApacheModuleConfigurationToApacheHttpdServerMapper(Change<Deployment> change) {
		super(change, false);
	}

	@Override
	protected void generateAdditionStepsForAddedMapping(ModJkApacheModuleConfiguration newMappingSource, ModJkApacheModuleConfigurationMapping newMapping, ApacheHttpdServer newMappingTarget, List<Step> steps) {
		steps.add(new CreateModkJKConfigFileStep(newMappingTarget, newMappingSource, newMapping));
		steps.add(new RestartApacheHttpdServerStep(newMappingTarget));
	}

	@Override
	protected void generateDeletionStepsForDeletedMapping(ModJkApacheModuleConfiguration oldMappingSource, ModJkApacheModuleConfigurationMapping oldVersionOfModifiedMapping, ApacheHttpdServer oldMappingTarget, List<Step> steps) {
		steps.add(new DeleteModkJKConfigFileStep(oldMappingTarget, oldMappingSource, oldVersionOfModifiedMapping));
		steps.add(new RestartApacheHttpdServerStep(oldMappingTarget));
	}
}
