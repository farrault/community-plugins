package com.xebialabs.deployit.plugin.cloudburst;

import java.util.List;

import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.Deployment;
import com.xebialabs.deployit.ci.artifact.Ear;
import com.xebialabs.deployit.mapper.ModificationSupportingStepGeneratingMapper;

public class EarToCloudBurstApplianceMapper extends ModificationSupportingStepGeneratingMapper<Ear, EarToCloudBurstApplianceMapping, CloudBurstAppliance> {

	public EarToCloudBurstApplianceMapper(Change<Deployment> change) {
		super(change, false);
	}

	@Override
	protected void generateAdditionStepsForAddedMapping(Ear ear, EarToCloudBurstApplianceMapping mapping, CloudBurstAppliance appliance, List<Step> steps) {
		String patternName = mapping.getPatternName();
		String cloudName = mapping.getCloudGroupName();
		String systemName = mapping.getSystemName();
		String systemPassword = mapping.getSystemPassword();

		steps.add(new CreateVirtualSystemStep(appliance, patternName, cloudName, systemName, systemPassword));
		steps.add(new ReadVirtualSystemStep(appliance, systemName));
		steps.add(new DiscoverWasUnmanagedServerOnVirtualSystemStep(systemName, systemPassword));
		steps.add(new DeployAndStartEarToDiscoveredWasUnmanagedServerOnVirtualSystemStep(ear, systemName));
	}

	@Override
	protected void generateModificationStepsForModifiedMapping(Ear oldMappingSource, EarToCloudBurstApplianceMapping oldVersionOfModifiedMapping,
	        CloudBurstAppliance oldMappingTarget, Ear newMappingSource, EarToCloudBurstApplianceMapping newVersionOfModifiedMapping,
	        CloudBurstAppliance newMappingTarget, List<Step> steps) {
		// Upgrade is not supported but we do not want a destroy/create to happen on upgrade, so it is explictly disable by 
	}

	@Override
	protected void generateDeletionStepsForDeletedMapping(Ear ear, EarToCloudBurstApplianceMapping mapping, CloudBurstAppliance appliance, List<Step> steps) {
		String systemName = mapping.getSystemName();
		steps.add(new DestroyVirtualSystemStep(appliance, systemName));

	}

}
