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

import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.ChangePlan;
import com.xebialabs.deployit.RunBook;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.Deployment;
import com.xebialabs.deployit.plugin.apache.httpd.mapper.StaticContentToApacheHttpdServerMapper;
import com.xebialabs.deployit.plugin.apache.modjk.mapper.ModJkApacheModuleConfigurationToApacheHttpdServerMapper;
import com.xebialabs.deployit.util.SingleTypeHandlingRunBook;

import java.util.List;

public class ModJkApacheModuleRunbook extends SingleTypeHandlingRunBook<Deployment> implements RunBook {

	public ModJkApacheModuleRunbook() {
		super(Deployment.class);
	}

	@Override
	protected void resolve(Change<Deployment> change, ChangePlan changePlan, List<Step> steps) {
		try {
			ModJkApacheModuleConfigurationToApacheHttpdServerMapper pluginConfigMapper = new ModJkApacheModuleConfigurationToApacheHttpdServerMapper(change);
			StaticContentToApacheHttpdServerMapper staticContentMapper = new StaticContentToApacheHttpdServerMapper(change);

			staticContentMapper.generateDeletionSteps(steps);
			pluginConfigMapper.generateDeletionSteps(steps);

			staticContentMapper.generateAdditionSteps(steps);
			pluginConfigMapper.generateAdditionSteps(steps);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
