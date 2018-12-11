/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.jsbridge.client;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.QualifierUtil;
import org.jboss.errai.ioc.client.container.SyncBeanDef;

public class DependentBeanDefinition<T, B extends T> implements SyncBeanDef<T> {

    private final Supplier<B> factory;
    private final Class<T> type;
    private final Set<Annotation> qualifiers;
    private final String name;
    private final boolean activated;
    private final Set<Class<?>> assignableTypes = new HashSet<>();

    public DependentBeanDefinition(final Supplier<B> factory,
                                   final Class<T> type,
                                   final Set<Annotation> qualifiers,
                                   final String name,
                                   final boolean activated,
                                   final Class<?>... otherAssignableTypes) {
        this.factory = factory;
        this.type = type;
        this.qualifiers = qualifiers;
        this.name = name;
        this.activated = activated;
        assignableTypes.add(type);
        assignableTypes.addAll(Arrays.asList(otherAssignableTypes));
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Class<?> getBeanClass() {
        return type;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public T getInstance() {
        return factory.get();
    }

    @Override
    public T newInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers == null ? Collections.emptySet() : qualifiers;
    }

    @Override
    public boolean matches(final Set<Annotation> annotations) {
        return QualifierUtil.matches(annotations, getQualifiers());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    @Override
    public boolean isAssignableTo(final Class<?> type) {
        return assignableTypes.contains(type);
    }
}
