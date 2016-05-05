package org.livespark.formmodeler.renderer.client.rendering.renderers.relations.multipleSubform;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.TableColumnMeta;
import org.livespark.formmodeler.renderer.client.DynamicFormRenderer;
import org.livespark.formmodeler.renderer.client.rendering.renderers.relations.multipleSubform.columns.ColumnGenerator;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.widgets.crud.client.component.CrudActionsHelper;
import org.livespark.widgets.crud.client.component.CrudComponent;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

@Templated
public class MultipleSubFormWidget extends Composite implements TakesValue<List<Object>> {

    public static final int PAGE_SIZE = 5;

    @Inject
    @DataField
    private FlowPanel content;

    @Inject
    protected ColumnGeneratorManager columnGeneratorManager;

    @Inject
    protected DynamicFormRenderer formRenderer;

    @Inject
    protected CrudComponent  crudComponent;

    private MultipleSubFormFieldDefinition field;

    private FormRenderingContext renderingContext;

    private AsyncDataProvider<HasProperties> dataProvider;

    private List<Object> values = null;
    private List<HasProperties> tableValues = new ArrayList<>();

    protected void init( FieldDefinition field ) {
        content.clear();
        content.add( crudComponent );
    }

    protected void initCrud() {
        final List<ColumnMeta> metas = new ArrayList<ColumnMeta>();

        BindableProxy<?> proxy = null;

        try {
            proxy = BindableProxyFactory.getBindableProxy( field.getStandaloneClassName() );
        } catch ( Exception e ) {
            GWT.log( "Unable to find proxy for type '" + field.getStandaloneClassName() + ".");
        }

        for ( TableColumnMeta meta : field.getColumnMetas() ) {

            String type = String.class.getName();

            if ( proxy != null ) {
                type = proxy.getBeanProperties().get( meta.getProperty() ).getType().getName();
            }

            ColumnGenerator generator = columnGeneratorManager.getGeneratorByType( type );

            if ( generator != null ) {

                ColumnMeta<HasProperties> columnMeta = new ColumnMeta<HasProperties>( generator.getColumn( meta.getProperty() ), meta.getLabel() );

                metas.add( columnMeta );
            }
        }

        dataProvider = new AsyncDataProvider<HasProperties>() {
            @Override
            protected void onRangeChanged( HasData<HasProperties> hasData ) {
                if ( tableValues != null ) {
                    updateRowCount( tableValues.size(), true );
                    updateRowData( 0, tableValues );
                } else {
                    updateRowCount( 0, true );
                    updateRowData( 0, new ArrayList<HasProperties>(  ) );
                }
            }
        };

        crudComponent.init( new CrudActionsHelper() {
            private Integer position;

            @Override
            public int getPageSize() {
                return PAGE_SIZE;
            }

            @Override
            public boolean showEmbeddedForms() {
                return true;
            }

            @Override
            public boolean isAllowCreate() {
                return !field.getReadonly();
            }

            @Override
            public boolean isAllowEdit() {
                return !field.getReadonly();
            }

            @Override
            public boolean isAllowDelete() {
                return !field.getReadonly();
            }

            @Override
            public List<ColumnMeta> getGridColumns() {
                return metas;
            }

            @Override
            public AsyncDataProvider getDataProvider() {
                return dataProvider;
            }

            @Override
            public IsFormView<Object> getCreateInstanceForm() {
                if ( field.getCreationForm() != null ) {
                    BindableProxy<?> proxy = null;
                    try {
                        proxy = BindableProxyFactory.getBindableProxy( field.getStandaloneClassName() );
                    } catch ( Exception e ) {
                        GWT.log( "Unable to find proxy for type '" + field.getStandaloneClassName() + ".");
                    }
                    formRenderer.render( renderingContext.getCopyFor( field.getCreationForm(), proxy ) );
                    return formRenderer;
                }

                return null;
            }

            @Override
            public IsFormView<Object> getEditInstanceForm( Integer position ) {
                this.position = position;
                if ( field.getEditionForm() != null ) {
                    formRenderer.render( renderingContext.getCopyFor( field.getCreationForm(), values.get( position ) ) );
                    return formRenderer;
                }

                return null;
            }

            @Override
            public void createInstance() {
                values.add( formRenderer.getModel() );
                tableValues.add( (HasProperties) formRenderer.getModel() );
                refreshCrud();
            }

            @Override
            public void editInstance() {
                values.set( position, formRenderer.getModel() );
                tableValues.set( position, (HasProperties) formRenderer.getModel() );
                refreshCrud();
            }

            @Override
            public void deleteInstance( int index ) {
                values.remove( index );
                tableValues.remove( index );
                refreshCrud();
            }
        } );
        initValues();
    }

    protected void initValues() {

        tableValues.clear();

        if ( values != null ) {
            for ( Object value : values ) {
                HasProperties tableValue;

                if ( value instanceof HasProperties ) {
                    tableValue = (HasProperties) value;
                } else {
                    tableValue = (HasProperties) DataBinder.forModel( value ).getModel();
                }

                tableValues.add( tableValue );
            }
        }
    }

    public void config( MultipleSubFormFieldDefinition field, FormRenderingContext renderingContext ) {
        init( field );

        this.field = field;
        this.renderingContext = renderingContext;

        initCrud();
    }

    protected void refreshCrud() {
        int currentStart = crudComponent.getCurrentPage();
        if ( currentStart < 0 ) {
            currentStart = 0;
        } else if( currentStart <= tableValues.size()) {
            currentStart -= PAGE_SIZE;
        }
        dataProvider.updateRowCount( tableValues.size(), true );
        dataProvider.updateRowData( currentStart, tableValues );
        crudComponent.refresh();
    }

    @Override
    public void setValue( List<Object> objects ) {
        // Avoid setting value via errai-data-binding when list is updated.
        if ( values != null ) {
            return;
        }
        values = objects;

        initValues();

        refreshCrud();
    }

    @Override
    public List<Object> getValue() {
        return values;
    }
}
