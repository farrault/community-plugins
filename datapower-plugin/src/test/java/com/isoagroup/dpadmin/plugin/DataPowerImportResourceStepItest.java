package com.isoagroup.dpadmin.plugin;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.isoagroup.dpadmin.deployit.plugin.DataPowerAppliance;
import com.isoagroup.dpadmin.deployit.plugin.DataPowerImportResourceStep;
import com.isoagroup.dpadmin.deployit.plugin.DataPowerResource;
import com.isoagroup.dpadmin.deployit.plugin.DataPowerResourceToApplianceMapping;
import com.isoagroup.dpadmin.deployit.plugin.DebugStepExecutionContext;
import com.xebialabs.deployit.ci.Host;

public class DataPowerImportResourceStepItest {

	@Test
	public void shouldImportDomain() {
		System.setProperty("com.xebia.ad.donotcleanuptemporaryfiles", "true");

		DataPowerAppliance appliance = new DataPowerAppliance();
		appliance.setLabel("appliance #1");
		appliance.setDeviceAlias("blue");
		appliance.setDpadminHomePath("c:\\dpadmin\\dpadmin\\");
		appliance.setDpadminHost(Host.getLocalHost());

		DataPowerResource export = new DataPowerResource();
		export.setLabel("export #1");
		export.setDomainName("TargetDomainNode2");
		export.setXmlManagerName("default");
		export.setLocation("c:\\Temp\\amiToMdm_request.xsl");

		DataPowerResourceToApplianceMapping mapping = new DataPowerResourceToApplianceMapping();
		mapping.setLabel("mapping #1");
		mapping.setSource(export);
		mapping.setTarget(appliance);
		mapping.setFlowName("deployitFlow1");
		mapping.setTargetDirectory("local:///commonFiles/xslt/");

		DataPowerImportResourceStep importStep = new DataPowerImportResourceStep(mapping);
		boolean success = importStep.execute(new DebugStepExecutionContext());
		assertTrue(success);
	}
}
