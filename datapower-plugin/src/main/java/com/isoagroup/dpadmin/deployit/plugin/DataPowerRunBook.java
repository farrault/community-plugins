package com.isoagroup.dpadmin.deployit.plugin;

import java.util.List;

import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.ChangePlan;
import com.xebialabs.deployit.RunBook;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.Deployment;
import com.xebialabs.deployit.util.SingleTypeHandlingRunBook;

public class DataPowerRunBook extends SingleTypeHandlingRunBook<Deployment> implements RunBook {

	public DataPowerRunBook() {
		super(Deployment.class);
	}

	@Override
	protected void resolve(Change<Deployment> c, ChangePlan cp, List<Step> steps) {
		if (c.isAddition()) {
			Deployment d = c.getNewRevision();
			List<DataPowerConfigToApplianceMapping> domainsToImport = d.getMappingsOfType(DataPowerConfigToApplianceMapping.class);
			for (DataPowerConfigToApplianceMapping each : domainsToImport) {
				steps.add(new DataPowerImportConfigStep(each));
			}

			List<DataPowerResourceToApplianceMapping> resourceToImport = d.getMappingsOfType(DataPowerResourceToApplianceMapping.class);
			for (DataPowerResourceToApplianceMapping each : resourceToImport) {
				steps.add(new DataPowerImportResourceStep(each));
			}
		}

	}

}
