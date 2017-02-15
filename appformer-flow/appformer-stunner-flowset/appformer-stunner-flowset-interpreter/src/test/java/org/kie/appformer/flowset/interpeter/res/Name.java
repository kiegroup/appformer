/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.interpeter.res;

import org.kie.appformer.flowset.interpreter.ModelOracle;

public class Name {

    private String first, last;

    public String getFirst() {
        return first;
    }

    public void setFirst( final String first ) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast( final String last ) {
        this.last = last;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((last == null) ? 0 : last.hashCode());
        return result;
    }

    @Override
    public boolean equals( final Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Name other = (Name) obj;
        if ( first == null ) {
            if ( other.first != null ) return false;
        } else if ( !first.equals( other.first ) ) return false;
        if ( last == null ) {
            if ( other.last != null ) return false;
        } else if ( !last.equals( other.last ) ) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Name [first=" + first + ", last=" + last + "]";
    }

    public static class NameOracle implements ModelOracle {

        @Override
        public void setProperty( final Object model,
                                 final String property,
                                 final Object value ) {
            final Name name = (Name) model;
            switch ( property ) {
                case "first":
                    name.setFirst( (String) value );
                    break;
                case "last":
                    name.setLast( (String) value );
                    break;
                default:
                    throw new IllegalArgumentException( "Bad property " + property );
            }
        }

        @Override
        public Object getProperty( final Object model,
                                   final String property ) {
            final Name name = (Name) model;
            switch ( property ) {
                case "first":
                    return name.getFirst();
                case "last":
                    return name.getLast();
                default:
                    throw new IllegalArgumentException( "Bad property " + property );
            }
        }

        @Override
        public Object createNestedModel( final Object model,
                                         final String property ) {
            throw new RuntimeException( "Not yet implemented." );
        }

        @Override
        public Object workingCopy( final Object model ) {
            throw new RuntimeException( "Not yet implemented." );
        }

        @Override
        public void mergeChanges( final Object original,
                                  final Object copy ) {
            throw new RuntimeException( "Not yet implemented." );
        }

    }

}
