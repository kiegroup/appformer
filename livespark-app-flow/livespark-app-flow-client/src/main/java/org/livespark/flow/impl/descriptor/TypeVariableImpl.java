/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.livespark.flow.impl.descriptor;

import java.util.Collections;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class TypeVariableImpl implements Type.TypeVariable {

    private final String name;
    private final List<Type> upperBounds;
    private final List<Type> lowerBounds;

    public TypeVariableImpl( final @MapsTo( "name" ) String name,
                             final @MapsTo( "upperBounds" ) List<Type> upperBounds,
                             final @MapsTo( "lowerBounds" ) List<Type> lowerBounds ) {
        this.name = name;
        this.upperBounds = Collections.unmodifiableList( upperBounds );
        this.lowerBounds = Collections.unmodifiableList( lowerBounds );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSimpleName() {
        return name;
    }

    @Override
    public List<Type> getUpperBounds() {
        return upperBounds;
    }

    @Override
    public List<Type> getLowerBounds() {
        return lowerBounds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append( name );

        lowerBounds
            .stream()
            .map( t -> t.toString() )
            .reduce( (s1, s2) -> s1 + " & " + s2 )
            .ifPresent( s -> sb.append( " super " ).append( s ) );

        upperBounds
            .stream()
            .map( t -> t.toString() )
            .reduce( (s1, s2) -> s1 + " & " + s2 )
            .ifPresent( s -> sb.append( " extends " ).append( s ) );

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ upperBounds.hashCode() ^ lowerBounds.hashCode();
    }

    @Override
    public boolean equals( final Object obj ) {
        return obj instanceof TypeVariable && equals( (TypeVariable) obj );
    }

}
