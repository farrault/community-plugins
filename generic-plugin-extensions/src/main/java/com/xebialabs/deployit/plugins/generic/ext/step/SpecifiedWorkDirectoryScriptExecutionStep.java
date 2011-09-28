/*
 * @(#)SpecifiedWorkDirectoryScriptExecutionStep.java     1 Sep 2011
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
package com.xebialabs.deployit.plugins.generic.ext.step;

import static java.lang.String.format;

import java.util.Map;

import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.step.ScriptExecutionStep;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

@SuppressWarnings("serial")
public class SpecifiedWorkDirectoryScriptExecutionStep extends ScriptExecutionStep {
    private final String specifiedWorkingDir;
    // XXX: perhaps make optional in future?
    private final boolean cleanupSpecifiedWorkingDir = true;
    private final String uploadedExecutableName;
    
    // no access to remoteWorkingDir in GenericBaseStep
    private transient OverthereFile specifiedRemoteWorkingDir;
    
    public SpecifiedWorkDirectoryScriptExecutionStep(int order,
            String scriptPath, Container container, Map<String, Object> vars,
            String description, String workingDirectory) {
        super(order, scriptPath, container, vars, description);
        specifiedWorkingDir = workingDirectory;
        uploadedExecutableName = substringAfterLast(getOsSpecificTemplatePath(scriptPath), '/');
    }

    @Override
    public Result doExecute() throws Exception {
        try {
            return super.doExecute();
        } finally {
            if ((specifiedWorkingDir != null) && cleanupSpecifiedWorkingDir) {
                deleteExecutedScript();
            }
            // if the script is re-run, the connection will need to be re-established
            specifiedRemoteWorkingDir = null;
        }
    }

    private void deleteExecutedScript() {
        OverthereFile remoteScriptFile = getRemoteWorkingDirectory().getFile(uploadedExecutableName);
        if (!remoteScriptFile.exists()) {
            getCtx().logOutput(format("WARNING: Executed script file '%s' not found for removal", 
                    remoteScriptFile.getPath()));
        } else {
            try {
                remoteScriptFile.delete();
            } catch (RuntimeIOException exception) {
                getCtx().logOutput(format("WARNING: Unable to delete script file '%s' due to: %s", 
                        remoteScriptFile.getPath(), exception));
            }
        }
    }

    @Override
    protected OverthereFile getRemoteWorkingDirectory() {
        if (specifiedWorkingDir == null) {
            return super.getRemoteWorkingDirectory();
        }
        
        // copied from GenericBaseStep
        if (specifiedRemoteWorkingDir == null) {
            specifiedRemoteWorkingDir = getRemoteConnection().getFile(specifiedWorkingDir);
            if (!specifiedRemoteWorkingDir.exists() || !specifiedRemoteWorkingDir.isDirectory()) {
                throw new IllegalArgumentException(format("Specified remote working directory '%s' does not exist or is not a directory", specifiedWorkingDir));
            }
        }
        return specifiedRemoteWorkingDir;
    }
    
    // adapted from ScriptExecutionStep.resolveOsSpecificTemplate
    private String getOsSpecificTemplatePath(String scriptTemplatePath) {
        String osSpecificScript = scriptTemplatePath;

        String scriptExt = substringAfterLast(scriptTemplatePath, '.');
        if (scriptExt == null) {
            OperatingSystemFamily os = getContainer().getHost().getOs();
            osSpecificScript = osSpecificScript + os.getScriptExtension();
        }

        return osSpecificScript;
    }

    // not accessible in ScriptExecutionStep...*sigh*
    private String substringAfterLast(String str, char sub) {
        int pos = str.lastIndexOf(sub);
        if(pos == -1) {
                return null;
        } else {
                return str.substring(pos + 1);
        }
    }
}
