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

import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfiguration;

import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModule;
import com.xebialabs.deployit.plugin.apache.modjk.ci.ModJkApacheModuleConfigurationMapping;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


public class GenerateManifest {

	@Test
	public void testGenerateManifest() throws Exception {

		Manifest m = new Manifest();
		final Attributes mainAttributes = m.getMainAttributes();
		mainAttributes.putValue("Manifest-Version", "1.0");
		final Map<String, Attributes> entries = m.getEntries();


		ModJkApacheModuleRunbook runbook = new ModJkApacheModuleRunbook();
		Attributes attributes = new Attributes();
		attributes.putValue("RunBook", "true");
		entries.put(toClassFile(runbook.getClass()), attributes);

		addCI(entries, ModJkApacheModuleConfiguration.class);
		addCI(entries, ModJkApacheModuleConfigurationMapping.class);
		addCI(entries, ModJkApacheModule.class);

		dumpManifest(m);
	}

	private void addCI(Map<String, Attributes> entries, Class clazz) {
		Attributes attributesci = new Attributes();
		attributesci.putValue("ConfigurationItem", "true");
		entries.put(toClassFile(clazz), attributesci);
	}

	String toClassFile(Class c) {
		String work = c.toString();
		return work.substring(6).replace('.', '/') + ".class";
	}

	private void dumpManifest(Manifest m) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			m.write(baos);
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		System.out.println(new String(baos.toByteArray()));
	}

}
