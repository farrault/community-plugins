/*
 * @(#)SingleTypeContributor.java     1 Sep 2011
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
package com.xebialabs.deployit.plugins.generic.ext.planning;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newLinkedList;
import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.CREATE;
import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.DESTROY;
import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.MODIFY;
import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.NOOP;
import static com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry.getSubtypes;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployed;

public abstract class SingleTypeContributor<D extends Deployed<?, ?>> {
    protected final Predicate<Delta> isOfType;
    
    protected List<D> deployedsCreated;
    protected List<TypedDelta> deployedsModified;
    protected List<TypedDelta> deployedsNoop;
    protected List<D> deployedsRemoved;

    private final Function<Delta, TypedDelta> toTypedDelta =
        new Function<Delta, TypedDelta>() {
        @Override
        public TypedDelta apply(Delta input) {
            return new TypedDelta(input);
        }
    };

    protected SingleTypeContributor(Class<? extends D> classOfDeployed) {
        isOfType = new IsSubtypeOf(Type.valueOf(classOfDeployed));
    }
    
    protected void filterDeltas(List<Delta> deltas) {
        deployedsCreated = newLinkedList(transform(
                filter(deltas, and(isOfType, operationIs(CREATE))),
                new Function<Delta, D>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public D apply(Delta input) {
                        return (D) input.getDeployed();
                    }
                }));
        deployedsModified = newLinkedList(transform(
                filter(deltas, and(isOfType, operationIs(MODIFY))),
                                               toTypedDelta));
        deployedsNoop = newLinkedList(transform(
                filter(deltas, and(isOfType, operationIs(NOOP))),
                                               toTypedDelta));
        deployedsRemoved = newLinkedList(transform(
                filter(deltas, and(isOfType, operationIs(DESTROY))), 
                new Function<Delta, D>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public D apply(Delta input) {
                        return (D) input.getPrevious();
                    }
                }));
    }
    
    protected static OperationEquals operationIs(Operation operationToMatch) {
        return new OperationEquals(operationToMatch);
    }
    
    protected static class OperationEquals implements Predicate<Delta> {
        private final Operation operationToMatch;
        
        protected OperationEquals(Operation operationToMatch) {
            this.operationToMatch = operationToMatch;
        }

        @Override
        public boolean apply(Delta input) {
            return input.getOperation().equals(operationToMatch);
        }
    }
    
    public static class IsSubtypeOf implements Predicate<Delta> {
        private final Collection<Type> subtypes;
        
        public IsSubtypeOf(Type typeToMatch) {
            subtypes = getSubtypes(typeToMatch);
            subtypes.add(typeToMatch);
        }

        @Override
        public boolean apply(Delta input) {
            return subtypes.contains(getType(input));
        }
        
        // move to DeltaUtils or whatever
        private static Type getType(Delta delta) {
            return (delta.getOperation().equals(DESTROY) 
                    ? delta.getPrevious().getType() 
                    : delta.getDeployed().getType());
        }
    }
    
    protected class TypedDelta implements Delta {
        private final Delta delegate;
        
        private TypedDelta(Delta delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public Operation getOperation() {
            return delegate.getOperation();
        }

        @SuppressWarnings("unchecked")
        @Override
        public D getPrevious() {
            return (D) delegate.getPrevious();
        }

        @SuppressWarnings("unchecked")
        @Override
        public D getDeployed() {
            return (D) delegate.getDeployed();
        }
    }
}
