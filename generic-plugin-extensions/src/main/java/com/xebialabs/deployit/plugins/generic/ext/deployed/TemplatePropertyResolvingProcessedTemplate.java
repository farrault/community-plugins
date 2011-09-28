/*
 * @(#)PropertyResolvingCopiedArtifact.java     17 Sep 2011
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
package com.xebialabs.deployit.plugins.generic.ext.deployed;

import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.generic.deployed.ProcessedTemplate;

@SuppressWarnings("serial")
public class TemplatePropertyResolvingProcessedTemplate<D extends Deployable> extends ProcessedTemplate<D> {

    @Override
    public String getTemplate() {
        // the parent implementation does not support placeholders
        return resolveExpression(super.getTemplate());
    }
}
