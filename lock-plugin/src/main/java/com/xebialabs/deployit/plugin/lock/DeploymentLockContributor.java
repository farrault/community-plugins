package com.xebialabs.deployit.plugin.lock;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;

import java.util.HashSet;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.Environment;

/**
 * Write all in Java, it is cross-platform Create lock.Manager CI that can list logs and clear all locks (control tasks) Default is to use locking for each host
 * (find hostcontainer), can be turned off with synthetic property
 */
public class DeploymentLockContributor {
	private static final String CONTAINER_CHECK_REQUIRED_PROPERTY = "allowConcurrentDeployments";
	
	//TODO: to make the below order configurable. Where can it be specified??
	private static final int CONTAINER_CHECK_ORDER = 2;
	private static final int CONTAINER_CHECK_CLEANUP_ORDER = 98;

	public DeploymentLockContributor() {
	}

	@Contributor
	public void addDeploymentLockCheckStep(Deltas deltas, DeploymentPlanningContext ctx) {
		Environment environment = ctx.getDeployedApplication().getEnvironment();

		// if the environment has been specified to be locked, no need to lock the individual containers
		if (FALSE.equals(environment.getProperty(CONTAINER_CHECK_REQUIRED_PROPERTY))) {
			ctx.addStep(new CheckAndCreateLockStep(CONTAINER_CHECK_ORDER, environment));
			ctx.addStep(new RemoveLockStep(CONTAINER_CHECK_CLEANUP_ORDER, environment));
		} else {
			Container[] containersRequiringCheck = getContainersRequiringCheck(deltas);
			ctx.addStep(new CheckAndCreateLockStep(CONTAINER_CHECK_ORDER, containersRequiringCheck));
			ctx.addStep(new RemoveLockStep(CONTAINER_CHECK_CLEANUP_ORDER, containersRequiringCheck));
		}
	}

	private Container[] getContainersRequiringCheck(Deltas deltas) {
		Iterable<Container> containersInAction = transform(deltas.getDeltas(), new Function<Delta, Container>() {
			@Override
			public Container apply(Delta input) {
				return (input.getOperation() == Operation.DESTROY ? input.getPrevious().getContainer() : input.getDeployed().getContainer());
			}
		});

		HashSet<Container> containers = newHashSet(filter(containersInAction, new Predicate<Container>() {
			@Override
			public boolean apply(Container input) {
				// may be null
				return FALSE.equals(input.getProperty(CONTAINER_CHECK_REQUIRED_PROPERTY));
			}
		}));
		return containers.toArray(new Container[containers.size()]);
	}
}
