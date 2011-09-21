package com.xebialabs.deployit.plugin.cloudburst;

import java.util.List;

import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.ChangePlan;
import com.xebialabs.deployit.RunBook;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.Deployment;
import com.xebialabs.deployit.util.SingleTypeHandlingRunBook;

public class CloudBurstApplianceRunBook extends SingleTypeHandlingRunBook<Deployment> implements RunBook {

	public CloudBurstApplianceRunBook() {
		super(Deployment.class);
	}

	@Override
	protected void resolve(Change<Deployment> change, ChangePlan changePlan, List<Step> steps) {
		EarToCloudBurstApplianceMapper mapper = new EarToCloudBurstApplianceMapper(change);

		mapper.generateDeletionSteps(steps);
		mapper.generateModificationSteps(steps);
		mapper.generateAdditionSteps(steps);
	}

}
