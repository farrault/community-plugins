package com.isoagroup.dpadmin.plugin;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.isoagroup.dpadmin.deployit.plugin.DataPowerAppliance;
import com.isoagroup.dpadmin.deployit.plugin.DataPowerConfig;
import com.isoagroup.dpadmin.deployit.plugin.DataPowerConfigToApplianceMapping;
import com.isoagroup.dpadmin.deployit.plugin.DataPowerImportConfigStep;
import com.isoagroup.dpadmin.deployit.plugin.DebugStepExecutionContext;
import com.xebialabs.deployit.ci.Host;

public class DataPowerImportConfigStepItest {

	@Test
	public void shouldImportDomain() {
		System.setProperty("com.xebia.ad.donotcleanuptemporaryfiles", "true");

		DataPowerAppliance appliance = new DataPowerAppliance();
		appliance.setLabel("appliance #1");
		appliance.setDeviceAlias("blue");
		appliance.setDpadminHomePath("c:\\dpadmin\\dpadmin\\");
		appliance.setDpadminHost(Host.getLocalHost());

		DataPowerConfig export = new DataPowerConfig();
		export.setLabel("export #1");
		export.setDomainName("TargetDomainNode2");
		export.setXmlManagerName("default");
		export.setLocation("C:\\temp\\DemoSource.zip");

		DataPowerConfigToApplianceMapping mapping = new DataPowerConfigToApplianceMapping();
		mapping.setLabel("mapping #1");
		mapping.setSource(export);
		mapping.setTarget(appliance);
		mapping.setFlowName("deployitFlow1");

		DataPowerImportConfigStep importStep = new DataPowerImportConfigStep(mapping);
		boolean success = importStep.execute(new DebugStepExecutionContext());
		assertTrue(success);
	}
}
