/*
 * @(#)ChangeTicket.java     8 Sep 2011
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
package com.xebialabs.deployit.plugins.changemgmt.deployed;

import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.generic.ci.Resource;
import com.xebialabs.deployit.plugin.generic.deployed.ExecutedScript;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "An Change ticket in a chg.ChangeManager")
public class ChangeTicket extends ExecutedScript<Resource> {
    private static final String UPDATE_SCRIPT_PROPERTY = "updateScript";
    private static final String UPDATE_ORDER_PROPERTY = "updateOrder";
    
    @Override
    public void executeCreate(DeploymentPlanningContext ctx, Delta d) {
        super.executeCreate(ctx, d);
        addStep(ctx, this.<Integer>getProperty(UPDATE_ORDER_PROPERTY), 
                this.<String>getProperty(UPDATE_SCRIPT_PROPERTY), "Update");
    }
}
