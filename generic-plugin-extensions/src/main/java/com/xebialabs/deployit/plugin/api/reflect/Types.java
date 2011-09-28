/*
 * @(#)Type2.java     14 Sep 2011
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
package com.xebialabs.deployit.plugin.api.reflect;

import java.util.Collection;

import com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry;
import com.xebialabs.deployit.plugin.api.reflect.Type;

public class Types {

    public static boolean isSubtypeOf(Type supertype, Type subtype) {
        Collection<Type> typeAndSubtypes = DescriptorRegistry.getSubtypes(supertype);
        typeAndSubtypes.add(supertype);
        return typeAndSubtypes.contains(subtype);
    }
}
