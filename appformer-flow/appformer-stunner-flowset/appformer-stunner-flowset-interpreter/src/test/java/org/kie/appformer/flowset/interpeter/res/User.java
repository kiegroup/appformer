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

public class User {

    private Address primary, mailing;
    private Name name;

    public Address getPrimary() {
        return primary;
    }
    public void setPrimary( final Address primary ) {
        this.primary = primary;
    }
    public Address getMailing() {
        return mailing;
    }
    public void setMailing( final Address mailing ) {
        this.mailing = mailing;
    }
    public Name getName() {
        return name;
    }
    public void setName( final Name name ) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mailing == null) ? 0 : mailing.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((primary == null) ? 0 : primary.hashCode());
        return result;
    }
    @Override
    public boolean equals( final Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final User other = (User) obj;
        if ( mailing == null ) {
            if ( other.mailing != null ) return false;
        } else if ( !mailing.equals( other.mailing ) ) return false;
        if ( name == null ) {
            if ( other.name != null ) return false;
        } else if ( !name.equals( other.name ) ) return false;
        if ( primary == null ) {
            if ( other.primary != null ) return false;
        } else if ( !primary.equals( other.primary ) ) return false;
        return true;
    }

    @Override
    public String toString() {
        return "User [primary=" + primary + ", mailing=" + mailing + ", name=" + name + "]";
    }

    public static class UserOracle implements ModelOracle {

        @Override
        public void setProperty( final Object model,
                                 final String property,
                                 final Object value ) {
            final User user = (User) model;
            switch ( property ) {
                case "name":
                    user.name = (Name) value;
                    break;
                case "primary":
                    user.primary = (Address) value;
                    break;
                case "mailing":
                    user.mailing = (Address) value;
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown property " + property );
            }
        }

        @Override
        public Object getProperty( final Object model,
                                   final String property ) {
            final User user = (User) model;
            switch ( property ) {
                case "name":
                    return user.name;
                case "primary":
                    return user.primary;
                case "mailing":
                    return user.mailing;
                default:
                    throw new IllegalArgumentException( "Unknown property " + property );
            }
        }

        @Override
        public Object createNestedModel( final Object model,
                                         final String property ) {
            final User user = (User) model;
            switch ( property ) {
                case "name":
                    user.setName( new Name() );
                    return user.getName();
                case "primary":
                    user.setPrimary( new Address() );
                    return user.getPrimary();
                case "mailing":
                    user.setMailing( new Address() );
                    return user.getMailing();
                default:
                    throw new IllegalArgumentException( "Unknown property " + property );
            }
        }

        @Override
        public Object workingCopy( final Object model ) {
            return model;
        }

        @Override
        public void mergeChanges( final Object original,
                                  final Object copy ) {
        }

    }

}
