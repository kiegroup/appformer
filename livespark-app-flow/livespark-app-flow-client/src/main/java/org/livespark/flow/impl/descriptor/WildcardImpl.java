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
public class WildcardImpl implements Type.Wildcard {

    private final List<Type> lowerBounds;
    private final List<Type> upperBounds;

    public WildcardImpl( final @MapsTo("lowerBounds") List<Type> lowerBounds, final @MapsTo("upperBounds") List<Type> upperBounds ) {
        this.lowerBounds = Collections.unmodifiableList( lowerBounds );
        this.upperBounds = Collections.unmodifiableList( upperBounds );
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
        sb.append( "?" );

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

}
