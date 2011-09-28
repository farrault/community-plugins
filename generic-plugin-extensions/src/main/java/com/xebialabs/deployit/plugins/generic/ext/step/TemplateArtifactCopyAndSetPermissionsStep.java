/*
 * @(#)TemplateArtifactCopyAndSetPermissionsStep.java     14 Sep 2011
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.util.OverthereFiles.setFilePermissions;

import java.util.Map;

import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.step.TemplateArtifactCopyStep;
import com.xebialabs.overthere.OperatingSystemFamily;

@SuppressWarnings("serial")
public class TemplateArtifactCopyAndSetPermissionsStep extends TemplateArtifactCopyStep {
    // not accessible in ArtifactCopyStep
    protected final String targetPath;
    private final String targetFilePermissions;

    public TemplateArtifactCopyAndSetPermissionsStep(int order,
            Container container, Map<String, Object> vars, String templatePath,
            String targetPath) {
        this(order, container, vars, templatePath, targetPath, null);
    }
    
    public TemplateArtifactCopyAndSetPermissionsStep(int order,
            Container container, Map<String, Object> vars, String templatePath,
            String targetPath, String targetFilePermissions) {
        super(order, container, vars, templatePath, targetPath);
        checkArgument(container.getHost().getOs().equals(OperatingSystemFamily.UNIX), 
                "File permissions can only be set on UNIX target hosts, but '%s' runs on %s machine '%s'", 
                container, container.getHost().getOs(), container.getHost());
        this.targetPath = targetPath;
        this.targetFilePermissions = targetFilePermissions;
    }

    @Override
    protected Result doExecute() throws Exception {
        Result result = super.doExecute();
        if (targetFilePermissions != null) {
            setFilePermissions(getRemoteConnection().getFile(targetPath)
                    .getFile(getTargetFileName()), targetFilePermissions);
        }
        return result;
    }

}
