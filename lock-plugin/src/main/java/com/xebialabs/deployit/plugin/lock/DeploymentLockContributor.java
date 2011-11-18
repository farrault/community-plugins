package com.xebialabs.deployit.plugin.lock;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.Container;

/**
 * Write all in Java, it is cross-platform
 * Create lock.Manager CI that can list logs and clear all locks (control tasks)
 * Default is to use locking for each host (find hostcontainer), can be turned off with synthetic property
 */
public class DeploymentLockContributor {
    private static final String CONTAINER_CHECK_REQUIRED_PROPERTY = "allowConcurrentDeployments";
    private static final String CONTAINER_CHECK_ORDER_PROPERTY = "deploymentInProgressCheckOrder";
    private static final String CONTAINER_CHECK_CLEANUP_ORDER_PROPERTY = "deploymentInProgressCheckCleanupOrder";
    
    public DeploymentLockContributor() {
    }
    
    @Contributor
    public void addDeploymentLockCheckSteps(Deltas deltas, DeploymentPlanningContext ctx) {
        for (Container container : getContainersRequiringCheck(deltas)) {
            ctx.addStep(new CheckAndCreateLockStep(
                    (Integer) container.getProperty(CONTAINER_CHECK_ORDER_PROPERTY),
                    container));

            ctx.addStep(new RemoveLockStep(
                    (Integer) container.getProperty(CONTAINER_CHECK_CLEANUP_ORDER_PROPERTY),
            		container));
        }
    }

    private Set<Container> getContainersRequiringCheck(Deltas deltas) {
        Iterable<Container> containersInAction =
        	transform(deltas.getDeltas(), new Function<Delta, Container>() {
                @Override
                public Container apply(Delta input) {
                	return (input.getOperation() == Operation.DESTROY ? 
                			input.getPrevious().getContainer() : 
                			input.getDeployed().getContainer()); 
                }
            });

        return newHashSet(filter(containersInAction, new Predicate<Container>() {
                @Override
                public boolean apply(Container input) {
                    // may be null
                    return FALSE.equals(input.getProperty(CONTAINER_CHECK_REQUIRED_PROPERTY));
                }
            }));
    }
}
