/*
 * @(#)Pojos.java     4 Sep 2011
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
package com.xebialabs.deployit.plugin.api;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import com.samskivert.mustache.Mustache;
import com.xebialabs.deployit.plugin.api.udm.artifact.DerivedArtifact;
import com.xebialabs.deployit.plugin.api.udm.artifact.PlaceholderReplacer;
import com.xebialabs.deployit.plugin.api.udm.artifact.SourceArtifact;

public class Pojos {
    private static final PlaceholderReplacer SIMPLE_REPLACER = new PlaceholderReplacer() {
            @Override
            public void replace(Reader in, Writer out, Map<String, String> resolution) {
                Mustache.compiler().compile(in).execute(resolution, out);
            }
        };
        
    public static void initDerivedArtifact(DerivedArtifact<? extends SourceArtifact> da) {
        if (da.getSourceArtifact() != null) {
            da.initFile(SIMPLE_REPLACER);
            checkArgument(da.getFile() != null, "After initialization DeployedArtifact %s has not set a file, cannot deploy.",
                    da.getId());
        }
    }
}
