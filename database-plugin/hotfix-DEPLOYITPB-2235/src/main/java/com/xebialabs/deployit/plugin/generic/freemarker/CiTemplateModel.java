/*
 * @(#)CiTemplateModel.java     1 Sep 2011
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
package com.xebialabs.deployit.plugin.generic.freemarker;

import com.xebialabs.deployit.plugin.api.reflect.Descriptor;
import com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry;
import com.xebialabs.deployit.plugin.api.reflect.PropertyDescriptor;
import com.xebialabs.deployit.plugin.api.reflect.PropertyKind;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.artifact.Artifact;
import com.xebialabs.deployit.plugin.generic.deployed.AbstractDeployed;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class CiTemplateModel extends BeanModel {
    private static final String FILE_ATTRIBUTE = "file";

    private Descriptor descriptor;
    private ConfigurationItem ci;
    private CiAwareObjectWrapper wrapper;

    public CiTemplateModel(ConfigurationItem ci, CiAwareObjectWrapper wrapper) {
        super(ci, wrapper);
        this.ci = ci;
        this.wrapper = wrapper;
        descriptor = DescriptorRegistry.getDescriptor(ci.getType());
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        if (key.equals(FILE_ATTRIBUTE) && ci instanceof Artifact) {
            return handleFile();
        }

        PropertyDescriptor pd = descriptor.getPropertyDescriptor(key);
        if (pd == null) {
            return super.get(key);
        } else {
            return wrapper.wrap(extractValueFromCi(pd));
        }
    }

    private Object extractValueFromCi(PropertyDescriptor pd) {
        if (pd.getKind() == PropertyKind.STRING && ci instanceof AbstractDeployed) {
            return ConfigurationHolder.resolveExpression((String) pd.get(ci),
                    ((AbstractDeployed<?>) ci).getDeployedAsFreeMarkerContext());
        }
        return pd.get(ci);
    }

    private TemplateModel handleFile() throws TemplateModelException {
        Artifact artifact = (Artifact) ci;
        Object o = artifact.getFile();
        if (wrapper.getUploader() != null) {
            o = wrapper.getUploader().upload(artifact.getFile());
        }

        return wrapper.wrap(o);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
