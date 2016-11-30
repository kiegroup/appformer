/*
 * Copyright 2015 JBoss Inc
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

package org.livespark.formmodeler.rendering.client.view.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.ui.IsWidget;

@Dependent
public class FormViewValidator {
    public static final String FORM_GROUP_SUFFIX = "_form_group";
    public static final String HELP_BLOCK_SUFFIX = "_help_block";

    protected Map<String, FieldGroup> formInputs = new HashMap<String, FieldGroup>();

    @Inject
    private Validator validator;

    public void registerInput( String fieldName, IsWidget widget ) {
        if ( fieldName != null && widget != null ){
            Element parent = findFormGroup( fieldName + FORM_GROUP_SUFFIX, widget.asWidget().getElement() );

            Element helpBlock = findHelpBlock( fieldName + HELP_BLOCK_SUFFIX, parent );

            FieldGroup group = new FieldGroup( fieldName, widget, parent, helpBlock );

            formInputs.put( fieldName, group );

        }
    }

    private Element findHelpBlock( String helpBlockId, Element parent ) {
        if ( parent == null ) return null;
        for ( int i=0; i<parent.getChildCount(); i++) {
            Node child = parent.getChild( i );
            if ( child.getNodeType() == Node.ELEMENT_NODE ) {
                Element childE = (Element) child;
                if ( childE.getId().equals( helpBlockId ) ) {
                    return childE;
                }
                childE = findHelpBlock( helpBlockId, childE );
                if ( childE != null ) return childE;
            }
        }
        return null;
    }

    protected Element findFormGroup( String groupId, Element element ) {
        if ( element.getTagName().equals( FormElement.TAG ) ) {
            return null;
        }
        if ( element.getId().equals( groupId ) ) {
            return element;
        }
        return findFormGroup( groupId, element.getParentElement() );
    }

    public void clearFieldErrors() {

        for ( FieldGroup group : formInputs.values() ) {
            if ( group.getFormGroup() != null )
                group.getFormGroup().removeClassName( "has-error" );
            if ( group.getHelpBlock() != null )
                group.getHelpBlock().setInnerHTML( "" );
        }

    }

    public boolean validate( Object model ) {
        boolean isValid = true;

        clearFieldErrors();

        try {
            Set<ConstraintViolation<Object>> result = validator.validate( model );

            for ( ConstraintViolation<Object> validation : result ) {
                String property = validation.getPropertyPath().toString().replace( ".", "_" );
                if ( !formInputs.containsKey( property ) )
                    continue;
                isValid = false;

                FieldGroup group = formInputs.get( property );

                if ( group.getFormGroup() != null )
                    group.getFormGroup().addClassName( "has-error" );
                if ( group.getHelpBlock() != null )
                    group.getHelpBlock().setInnerHTML( validation.getMessage() );
            }
        } catch ( IllegalArgumentException ex ) {
            GWT.log( "Error trying to validate model: model does not any validation constraint. " );
            return true;
        }

        return isValid;
    }

    private class FieldGroup {
        private String fieldName;
        private IsWidget widget;
        private Element formGroup;
        private Element helpBlock;

        public FieldGroup( String fieldName, IsWidget widget, Element formGroup, Element helpBlock ) {
            this.fieldName = fieldName;
            this.widget = widget;
            this.formGroup = formGroup;
            this.helpBlock = helpBlock;
        }

        public String getFieldName() {
            return fieldName;
        }

        public IsWidget getWidget() {
            return widget;
        }

        public Element getFormGroup() {
            return formGroup;
        }

        public Element getHelpBlock() {
            return helpBlock;
        }
    }
}
