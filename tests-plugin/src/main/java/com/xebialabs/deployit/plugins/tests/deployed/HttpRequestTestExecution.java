/*
 * @(#)ExecutedSqlScript.java     1 Sep 2011
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
package com.xebialabs.deployit.plugins.tests.deployed;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;

import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.generic.ci.Resource;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugins.generic.ext.deployed.ScriptPropertyResolvingExecutedScript;
import com.xebialabs.overthere.OperatingSystemFamily;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "An HTTP request test")
public class HttpRequestTestExecution extends ScriptPropertyResolvingExecutedScript<Resource> {

    @Override
    public void executeCreate(DeploymentPlanningContext ctx) {
        // only supporting execution on UNIX hosts at present
        checkArgument(getContainer().getHost().getOs().equals(OperatingSystemFamily.UNIX),
                "HTTP request test execution is only supported on UNIX hosts, but host OS family is '%s'",
                getContainer().getHost().getOs());
        super.executeCreate(ctx);
    }

    @Override
    public void executeDestroy(DeploymentPlanningContext ctx) {
        // do nothing - destroy not supported
    }
    
    public String getHostTemporaryDirectoryOrDefault() {
        Host host = getContainer().getHost();
        String hostTemporaryDirectory = nullToEmpty(host.getTemporaryDirectoryPath());
        return (!hostTemporaryDirectory.isEmpty() 
                ? hostTemporaryDirectory 
                : host.getOs().getDefaultTemporaryDirectoryPath());
    }
}
