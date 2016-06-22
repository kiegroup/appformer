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

package org.livespark.formmodeler.rendering.client.view.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.text.shared.Renderer;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.api.DataBinder;

/**
 * @author Pere Fernandez <pefernan@redhat.com>
 */
public class ObjectValuesRenderer<T> implements Renderer<T> {

    private List<MaskSection> sections = new ArrayList<>();

    public ObjectValuesRenderer() {
        this( "" );
    }

    public ObjectValuesRenderer( String mask ) {

        if ( isValid( mask ) ) {
            while ( mask.contains( "{" ) && mask.contains( "}" ) ) {
                int open = mask.indexOf( "{" );
                int close = mask.indexOf( "}" );

                String text = mask.substring( 0, open  );
                String property = mask.substring( open + 1, close );

                if ( !text.isEmpty() ) {
                    sections.add( new MaskSection( MaskSectionType.LITERAL, text ) );
                }

                sections.add( new MaskSection( MaskSectionType.PROPERTY, property ) );

                mask = mask.substring( close + 1 );
            }
            if ( !mask.isEmpty() ) {
                sections.add( new MaskSection( MaskSectionType.LITERAL, mask ) );
            }
        }
    }

    protected boolean isValid( String mask ) {
        if ( mask == null || mask.isEmpty() ) {
            return false;
        }

        int countOpeners = 0;

        int countClosers = 0;

        for ( char c : mask.toCharArray() ) {
            if ( c == '{') {
                countOpeners ++;
            } else if ( c == '}' ) {
                countClosers ++;
            }
        }

        return countOpeners != 0 && countOpeners == countClosers;
    }


    @Override
    public String render( T object ) {

        if ( object == null ) {
            return "";
        }

        if ( sections.isEmpty() ) {
            return object.toString();
        }

        HasProperties hasProperties;

        if ( object instanceof HasProperties ) {
            hasProperties = (HasProperties) object;
        } else {
            hasProperties = (HasProperties) DataBinder.forModel( object ).getModel();
        }

        String result = "";

        for ( MaskSection section : sections ) {
            String value = section.getText();
            if ( section.getType().equals( MaskSectionType.PROPERTY )) {
                Object propertyValue = hasProperties.get( section.getText() );
                if ( propertyValue != null ) {
                    value = propertyValue.toString();
                } else {
                    value = "";
                }
            }

            result += value;
        }

        return result;
    }

    @Override
    public void render( T object, Appendable appendable ) throws IOException {
        appendable.append( render( object ) );
    }

    private enum MaskSectionType {
        PROPERTY, LITERAL
    }

    private class MaskSection {
        private MaskSectionType type;
        private String text;

        public MaskSection( MaskSectionType type, String text ) {
            this.type = type;
            this.text = text;
        }

        public MaskSectionType getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
}
