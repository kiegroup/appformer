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
package org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionRequest;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionResponse;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event.FieldSelectorOptionUpdate;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.SelectorOption;
import org.uberfire.ext.properties.editor.client.fields.AbstractField;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;

@Dependent
public class SelectorOptionFormPresenter extends AbstractField {

    public interface SelectorOptionFormView extends IsWidget {
        public void setPresenter( SelectorOptionFormPresenter presenter );
        public void setOptions(List<SelectorOption> options);
    }

    public interface ParamsReader {
        public String getFormId( String params );
        public String getFieldId( String params );
    }

    protected SelectorOptionFormView view;

    protected Event<FieldSelectorOptionRequest> requestEvent;

    protected Event<FieldSelectorOptionUpdate> updateEvent;

    protected ParamsReader paramsReader;

    protected PropertyEditorFieldInfo property;

    protected String formId;

    protected String fieldId;

    private SelectorOption defaultValue;

    protected List<SelectorOption> options;

    @Inject
    public SelectorOptionFormPresenter( SelectorOptionFormView view,
                                        Event<FieldSelectorOptionRequest> requestEvent,
                                        Event<FieldSelectorOptionUpdate> updateEvent,
                                        ParamsReader paramsReader ) {
        this.view = view;
        this.requestEvent = requestEvent;
        this.updateEvent = updateEvent;
        this.paramsReader = paramsReader;
        this.view.setPresenter( this );
    }

    public void addOption( String value ) {
        options.add( new SelectorOption( value ) );
        view.setOptions( options );
        updateEvent.fire( new FieldSelectorOptionUpdate( formId, fieldId, options ) );
    }

    public void removeOption( SelectorOption option ) {
        options.remove( option );
        view.setOptions( options );
        updateEvent.fire( new FieldSelectorOptionUpdate( formId, fieldId, options ) );
        if ( defaultValue != null && defaultValue.equals( option ) ) defaultValue = null;
    }

    public void setDefaultValue( SelectorOption defaultValue ) {
        if ( this.defaultValue != null ) this.defaultValue.setDefaultValue( Boolean.FALSE );
        this.defaultValue = defaultValue;
        this.defaultValue.setDefaultValue( Boolean.TRUE );
        view.setOptions( options );
        updateEvent.fire( new FieldSelectorOptionUpdate( formId, fieldId, options ) );
    }

    public boolean existOption( String text ) {
        for ( SelectorOption option : options ) {
            if ( option.getValue().equals( text ) ) return true;
        }
        return false;
    }

    @Override
    public Widget widget( PropertyEditorFieldInfo property ) {
        this.property = property;

        this.formId = paramsReader.getFormId( property.getCurrentStringValue() );
        this.fieldId = paramsReader.getFieldId( property.getCurrentStringValue() );

        requestEvent.fire( new FieldSelectorOptionRequest( this.formId, this.fieldId ) );

        return view.asWidget();
    }

    protected void onResponse( @Observes FieldSelectorOptionResponse response ) {
        if ( this.formId.equals( response.getFormId() ) && this.fieldId.equals( response.getFieldId() ) ) {
            options = response.getOptions();

            if ( options == null ) options = new ArrayList<SelectorOption>();

            view.setOptions( options );

            for ( SelectorOption option : options ) {
                if ( option.getDefaultValue() ) {
                    defaultValue = option;
                    return;
                }
            }
        }
    }

    public SelectorOption getDefaultValue() {
        return defaultValue;
    }
}
