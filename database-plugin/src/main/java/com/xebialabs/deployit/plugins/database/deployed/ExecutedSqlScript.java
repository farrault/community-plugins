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
package com.xebialabs.deployit.plugins.database.deployed;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry.getDescriptor;
import static com.xebialabs.deployit.plugins.database.ci.SqlClient.*;

import java.util.Map;

import com.google.common.base.Function;
import com.xebialabs.deployit.plugin.api.Pojos;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.ci.File;
import com.xebialabs.deployit.plugins.database.ci.SqlClient;
import com.xebialabs.deployit.plugins.generic.ext.deployed.ExecutedScriptForCustomizableArtifact;
import com.xebialabs.deployit.plugins.generic.ext.step.SpecifiedWorkDirectoryScriptExecutionStep;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "An SQL script executed against an sql.SqlClient")
public class ExecutedSqlScript extends ExecutedScriptForCustomizableArtifact<File> {
    private static String SCHEMA_PROPERTY = "schema";
    private static String USERNAME_PROPERTY = "username";
    private static String PASSWORD_PROPERTY = "password";
    
    private static String SCRIPT_ROLLBACK_SCRIPT_PROPERTY = "rollbackScript";

    @Override
    public void executeCreate(DeploymentPlanningContext ctx) {
        // disable - needs to be ordered by the SqlScriptOrderer 
    }
    
    // called from SqlScriptOrderer    
    public void runSqlScript(DeploymentPlanningContext ctx) {
        super.executeCreate(ctx);
    }

    @Override
    public void executeModify(DeploymentPlanningContext ctx) {
        // disable the broken parent implementation
    }
    
    // called from SqlScriptOrderer
    public void updateSqlScript(DeploymentPlanningContext ctx) {
        // modify simply runs the new script - assumed to be incremental 
        runSqlScript(ctx);
    }

    @Override
    public void executeDestroy(DeploymentPlanningContext ctx) {
        // disable - needs to be ordered by the SqlScriptOrderer
    }

    // called from SqlScriptOrderer    
    public void runRollbackSqlScript(DeploymentPlanningContext ctx) {
        File rollbackScript = getDeployable().getSyntheticProperty(SCRIPT_ROLLBACK_SCRIPT_PROPERTY);
        if (rollbackScript != null) {
            // running a rollback script is running a regular script at a different order
            ExecutedSqlScript delegate = getDelegate();
            delegate.setDeployable(rollbackScript);

            //delegate.setFile(rollbackScript.getFile());
            Pojos.initDerivedArtifact(delegate);
            delegate.setCreateOrder(getDestroyOrder());
            delegate.runSqlScript(ctx);
        }
    }
    
    private ExecutedSqlScript getDelegate() {
        ExecutedSqlScript delegate = getDescriptor(getType()).newInstance();
        delegate.setContainer(getContainer());
        delegate.putSyntheticProperty(SCHEMA_PROPERTY, getSyntheticProperty(SCHEMA_PROPERTY));
        delegate.putSyntheticProperty(USERNAME_PROPERTY, getSyntheticProperty(USERNAME_PROPERTY));
        delegate.putSyntheticProperty(PASSWORD_PROPERTY, getSyntheticProperty(PASSWORD_PROPERTY));
        delegate.setPlaceholders(newHashMap(getPlaceholders()));
        return delegate;
    }

    @Override
    protected boolean addStep(DeploymentPlanningContext ctx, int order, String script, String verb) {
        if (!nullToEmpty(script).trim().isEmpty()) {
            ctx.addStep(new SpecifiedWorkDirectoryScriptExecutionStep(order, script, 
                    getContainer(), getDeployedAsFreeMarkerContext(), getDescription(verb),
                    emptyToNull((String) getContainer().getSyntheticProperty(WORK_DIRECTORY_PROPERTY))));
            return true;
        } else {
            return false;
        }
    }

    public String getResolvedCommandLine() {
        return resolveExpression((String) getContainer().getSyntheticProperty(COMMAND_PROPERTY));
    }
        
    public String getSchemaOrDefault() {
        return getValueOrClientDefault(SCHEMA_PROPERTY, DEFAULT_SCHEMA_PROPERTY);
    }
    
    public String getUsernameOrDefault() {
        return getValueOrClientDefault(USERNAME_PROPERTY, DEFAULT_USERNAME_PROPERTY);        
    }
    
    public String getPasswordOrDefault() {
        return getValueOrClientDefault(PASSWORD_PROPERTY, DEFAULT_PASSWORD_PROPERTY);        
    }

    // both may be empty!
    protected String getValueOrClientDefault(String property, String clientFallbackProperty) {
        String value = nullToEmpty((String) getSyntheticProperty(property));
        return ((!value.isEmpty()) ? value : nullToEmpty((String) getContainer().getSyntheticProperty(clientFallbackProperty)));
    }
    
    public Map<String, String> getResolvedEnvVars() {
        return transformValues(getClient().getEnvVars(), 
                new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return resolveExpression(input);
                    }
                });
    }
    
    private SqlClient getClient() {
        Container container = getContainer();
        checkState(container instanceof SqlClient, container.getClass());
        return (SqlClient) container;
    }
}
