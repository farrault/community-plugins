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
package com.xebialabs.deployit.plugins.database.ci;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.generic.ci.Container;

@SuppressWarnings("serial")
@Metadata(description = "A generic SQL client")
public class SqlClient extends Container  {
    public static String COMMAND_PROPERTY = "command";
    public static String DEFAULT_SCHEMA_PROPERTY = "defaultSchema";
    public static String DEFAULT_USERNAME_PROPERTY = "defaultUsername";
    public static String DEFAULT_PASSWORD_PROPERTY = "defaultPassword";
    public static String WORK_DIRECTORY_PROPERTY = "workingDirectory";

    @Property(required = false, description = "Environment variables to be set before calling the SQL client")
    private Map<String, String> envVars = newHashMap();
    
    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }
}
