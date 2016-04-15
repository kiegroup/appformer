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
package org.livespark.formmodeler.renderer.client;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.PropertyChangeUnsubscribeHandle;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeHandler;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.renderer.client.rendering.FieldLayoutComponent;
import org.livespark.formmodeler.renderer.client.rendering.renderers.relations.subform.SubFormWidget;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.formmodeler.renderer.service.Model2FormTransformerService;
import org.livespark.formmodeler.rendering.client.view.validation.FormViewValidator;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;

@Dependent
public class DynamicFormRenderer implements IsWidget, IsFormView {

    public interface DynamicFormRendererView extends IsWidget {
        void setPresenter( DynamicFormRenderer presenter );

        void render( FormRenderingContext context );
        void bind();

        FieldLayoutComponent getFieldLayoutComponentForField( FieldDefinition field );
    }

    private DynamicFormRendererView view;

    private Caller<Model2FormTransformerService> transformerService;

    private FormViewValidator formViewValidator;

    private DataBinder binder;

    private FormRenderingContext context;

    private List<PropertyChangeUnsubscribeHandle> unsubscribeHandlers = new ArrayList<PropertyChangeUnsubscribeHandle>();

    @Inject
    public DynamicFormRenderer( DynamicFormRendererView view, Caller<Model2FormTransformerService> transformerService, FormViewValidator formViewValidator ) {
        this.view = view;
        this.transformerService = transformerService;
        this.formViewValidator = formViewValidator;
    }

    @PostConstruct
    protected void init() {
        view.setPresenter( this );
    }

    public void renderDefaultForm( final Object model ) {
        transformerService.call( new RemoteCallback<FormRenderingContext>() {
            @Override
            public void callback( FormRenderingContext context ) {
                context.setModel( model );
                render( context );
            }
        } ).createContext( model );
    }

    public void render ( FormRenderingContext context ) {
        unBind();
        if ( context == null ) {
            return;
        }
        this.context = context;
        view.render( context );
        if ( context.getModel() != null ) {
            bind( context.getModel() );
        }
    }

    public void bind( Object model ) {
        if ( context != null && model != null ) {
            context.setModel( model );
            doBind( model );
            view.bind();
        }
    }

    protected void doBind( Object model ) {
        binder = DataBinder.forModel( context.getModel() );
    }

    public void bind( Widget input, FieldDefinition field ) {
        doBind( input, field );
    }

    protected void doBind( Widget input, FieldDefinition field ) {
        if ( isBinded() ) {
            binder.bind( input, field.getBindingExpression() );
            formViewValidator.registerInput( field.getName(), input );
        }
    }

    public void addPropertyChangeHandler( PropertyChangeHandler handler ) {
        addPropertyChangeHandler( null, handler );
    }

    public void addPropertyChangeHandler( String property, PropertyChangeHandler handler ) {
        if ( context != null && isBinded() ) {
            if ( property != null ) {

                boolean composed = property.indexOf( "." ) != -1;

                String root = property;
                String child = null;

                if ( composed ) {
                    int index = property.indexOf( "." );
                    root = property.substring( 0, index );

                    child = property.substring( index + 1 );
                }

                FieldDefinition field = context.getRootForm().getFieldByName( root );
                if ( field == null ) {
                    registerChangeHandler( property, handler );
                } else {
                    if ( field instanceof SubFormFieldDefinition ) {

                        FieldLayoutComponent component = view.getFieldLayoutComponentForField( field );

                        SubFormWidget subFormWidget = (SubFormWidget) component.getFieldRenderer().getInputWidget();

                        if ( composed ) {
                            subFormWidget.addPropertyChangeHandler( child, handler );
                        } else {
                            subFormWidget.addPropertyChangeHandler( handler );
                        }
                    } else {
                        registerChangeHandler( field.getBindingExpression(), handler );
                    }
                }
            } else {
                registerChangeHandler( null, handler );
            }
        }
    }

    protected void registerChangeHandler( String property, PropertyChangeHandler handler ) {
        if ( isBinded() ) {
            PropertyChangeUnsubscribeHandle unsubscribeHandle = doRegister( property, handler );

            unsubscribeHandlers.add( unsubscribeHandle );
        }
    }

    protected PropertyChangeUnsubscribeHandle doRegister( String property, PropertyChangeHandler handler ) {
        if ( property == null ) {
            return binder.addPropertyChangeHandler( handler );
        }
        return binder.addPropertyChangeHandler( property, handler );
    }

    public void unBind() {
        if ( isBinded() ) {
            doUnbind();
            for ( PropertyChangeUnsubscribeHandle handle : unsubscribeHandlers ) {
                handle.unsubscribe();
            }
            unsubscribeHandlers.clear();
            for ( FieldDefinition field : context.getRootForm().getFields() ) {
                if ( field instanceof SubFormFieldDefinition ) {
                    FieldLayoutComponent component = view.getFieldLayoutComponentForField( field );
                    SubFormWidget subFormWidget = (SubFormWidget) component.getFieldRenderer().getInputWidget();
                    subFormWidget.unBind();
                }
            }
        }
    }

    protected void doUnbind() {
        if( isBinded() ) {
            binder.unbind();
        }
    }

    @Override
    public void setModel( Object model ) {
        bind( model );
    }

    @Override
    public Object getModel() {
        if ( isBinded() ) {
            return binder.getModel();
        }
        return null;
    }

    public boolean isValid() {
        return formViewValidator.validate( getBinderModel() );
    }

    protected Object getBinderModel() {
        return binder.getModel();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    protected boolean isBinded() {
        return binder != null;
    }
}
