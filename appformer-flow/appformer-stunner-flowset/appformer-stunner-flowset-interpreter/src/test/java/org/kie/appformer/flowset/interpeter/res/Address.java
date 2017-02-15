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

public class Address {

    private String street, number;

    public String getStreet() {
        return street;
    }

    public void setStreet( final String street ) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber( final String number ) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((street == null) ? 0 : street.hashCode());
        return result;
    }

    @Override
    public boolean equals( final Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Address other = (Address) obj;
        if ( number == null ) {
            if ( other.number != null ) return false;
        } else if ( !number.equals( other.number ) ) return false;
        if ( street == null ) {
            if ( other.street != null ) return false;
        } else if ( !street.equals( other.street ) ) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Address [street=" + street + ", number=" + number + "]";
    }

    public static class AddressOracle implements ModelOracle {

        @Override
        public void setProperty( final Object model,
                                 final String property,
                                 final Object value ) {
            final Address address = (Address) model;
            switch ( property ) {
                case "street":
                    address.setStreet( (String) value );
                    break;
                case "number":
                    address.setNumber( (String) value );
                    break;
                default:
                    throw new IllegalArgumentException( "Bad property " + property );
            }
        }

        @Override
        public Object getProperty( final Object model,
                                   final String property ) {
            final Address address = (Address) model;
            switch ( property ) {
                case "street":
                    return address.getStreet();
                case "number":
                    return address.getNumber();
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
