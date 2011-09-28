/*
 * @(#)SqlScriptOrderer.java     1 Sep 2011
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
package com.xebialabs.deployit.plugins.database.planning;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.CREATE;
import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.MODIFY;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSortedMap;
import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.generic.ci.File;
import com.xebialabs.deployit.plugins.database.deployed.ExecutedSqlScript;
import com.xebialabs.deployit.plugins.generic.ext.planning.SingleTypeContributor;

public class SqlScriptOrderer extends SingleTypeContributor<ExecutedSqlScript> {

    public SqlScriptOrderer() {
        super(ExecutedSqlScript.class);
    }
    
    @Contributor
    public void orderSqlScripts(Deltas deltas, DeploymentPlanningContext ctx) {
        filterDeltas(deltas.getDeltas());
        
        ImmutableSortedMap<ExecutedSqlScript, Operation> sqlScriptsToRunInOrder =
            ImmutableSortedMap.copyOf(getSqlScriptsToRun(), new CompareOnContainerThenOrder(true));
        
        for (Entry<ExecutedSqlScript, Operation> sqlScriptToRun : sqlScriptsToRunInOrder.entrySet()) {
            switch(sqlScriptToRun.getValue()) {
            case CREATE:
                sqlScriptToRun.getKey().runSqlScript(ctx);
                break;
            case MODIFY:
                sqlScriptToRun.getKey().updateSqlScript(ctx);
                break;
            default:
                // should never happen
            }
        }
        
        // linked lists don't sort all that well
        List<ExecutedSqlScript> sqlScriptsToRollback = newArrayList(deployedsRemoved);

        // rollback scripts need to be run in *reverse* order
        Collections.sort(sqlScriptsToRollback, new CompareOnContainerThenOrder(false));
        
        for (ExecutedSqlScript sqlScriptToRollback : sqlScriptsToRollback) {
            sqlScriptToRollback.runRollbackSqlScript(ctx);
        }
    }       

    private Map<ExecutedSqlScript, Operation> getSqlScriptsToRun() {
        Builder<ExecutedSqlScript, Operation> sqlScriptsToRun = ImmutableMap.builder();
        for (ExecutedSqlScript newSqlScript : deployedsCreated) {
            sqlScriptsToRun.put(newSqlScript, CREATE);
        }
        for (TypedDelta updatedSqlScript : deployedsModified) {
            sqlScriptsToRun.put(updatedSqlScript.getDeployed(), MODIFY);
        }
        return sqlScriptsToRun.build();
    }
    
    private static class CompareOnContainerThenOrder implements Comparator<ExecutedSqlScript> {
        private static final String SCRIPT_ORDER_PROPERTY = "order";
        
        private final boolean sortAscending;
        
        private CompareOnContainerThenOrder(boolean sortAscending) {
            this.sortAscending = sortAscending;
        }

        @Override
        public int compare(ExecutedSqlScript o1, ExecutedSqlScript o2) {
            int containerComparison = o1.getContainer().compareTo(o2.getContainer());
            return (containerComparison != 0)
                    ? containerComparison
                    : ((sortAscending ? 1 : -1) * getOrder(o1).compareTo(getOrder(o2)));
        }
        
        private static Integer getOrder(ExecutedSqlScript sqlScript) {
            File deployable = sqlScript.getDeployable();
            checkState(deployable.getType().equals(Type.valueOf("sql.SqlScript")), deployable.getType());
            return deployable.getSyntheticProperty(SCRIPT_ORDER_PROPERTY);
        }
    }
}
