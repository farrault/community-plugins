/*
 * @(#)WebContent.java     18 Aug 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.plugins.webserver.deployed;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;

import com.xebialabs.deployit.plugin.api.deployment.execution.DeploymentStep;
import com.xebialabs.deployit.plugin.api.deployment.planning.Create;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.planning.Destroy;
import com.xebialabs.deployit.plugin.api.deployment.planning.Modify;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.generic.ci.Resource;
import com.xebialabs.deployit.plugins.generic.ext.deployed.TemplatePropertyResolvingProcessedTemplate;
import com.xebialabs.deployit.plugins.generic.ext.step.CommandExecutionStep;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "A configuration fragment deployed to a www.ApacheHttpdServer")
public class ApacheConfFragment extends TemplatePropertyResolvingProcessedTemplate<Resource> {
    private static final String RELOAD_COMMAND_PROPERTY = "reloadScript";

    @Override
    public void executeModify(DeploymentPlanningContext ctx) {
        // disable the broken parent implementation
    }
    
    @Modify
    public static void executeModify(DeploymentPlanningContext ctx, Delta delta) {
        ((ApacheConfFragment) delta.getPrevious()).executeDestroy(ctx);
        ApacheConfFragment confFragment = (ApacheConfFragment) delta.getDeployed();
        confFragment.executeCreate(ctx);
        confFragment.reloadServer(ctx);
    }

    @Create @Modify @Destroy
    public void reloadServer(DeploymentPlanningContext ctx) {
        addReloadSteps(ctx);
    }

    private void addReloadSteps(DeploymentPlanningContext ctx) {
        // neither reload nor restart are required!
        String reloadCommand = getReloadCommand();
        String restartCommand = nullToEmpty(getContainer().getRestartScript());
        if (!reloadCommand.isEmpty()) {
            ctx.addStep(executeCommandAtRestartOrder(reloadCommand));
            return;
        } else if (!restartCommand.isEmpty()) {
            ctx.addStep(executeCommandAtRestartOrder(restartCommand));
        } else {
            ctx.addSteps(executeCommandAtRestartOrder(getContainer().getStopScript()),
                    executeCommandAtRestartOrder(getContainer().getStartScript()));
        }
    }
    
    private DeploymentStep executeCommandAtRestartOrder(String command) {
        return new CommandExecutionStep(getContainer().getRestartOrder(), 
                getContainer().getHost(), command);
    }

    private String getReloadCommand() {
        return nullToEmpty((String) getContainer().getSyntheticProperty(RELOAD_COMMAND_PROPERTY));
    }
    
    @Override
    protected String getDescription(String verb) {
        return format("%s Apache configuration fragment '%s' in '%s' on %s", verb, getName(), 
                resolveTargetFileName(), getContainer().getName());
    }
}
