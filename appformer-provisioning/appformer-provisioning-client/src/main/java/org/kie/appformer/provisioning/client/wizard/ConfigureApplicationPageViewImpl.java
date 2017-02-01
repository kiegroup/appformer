/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.provisioning.client.wizard;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.Option;
import org.jboss.errai.common.client.dom.Select;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.appformer.provisioning.client.resources.i18n.AppFormerProvisioningConstants;
import org.uberfire.commons.data.Pair;

@Dependent
@Templated
public class ConfigureApplicationPageViewImpl
        implements ConfigureApplicationPageView, IsElement {

    @Inject
    @DataField( "datasource-selector" )
    private Select dataSourceSelector;

    @Inject
    private TranslationService translationService;

    private Presenter presenter;

    @Inject
    private Document doc;

    public ConfigureApplicationPageViewImpl( ) {
    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public String getPageTitle( ) {
        return translationService.getTranslation( AppFormerProvisioningConstants.ConfigureApplicationPage_title );
    }

    @Override
    public String getDataSource( ) {
        return dataSourceSelector.getValue( );
    }

    @Override
    public void setDataSource( String dataSource ) {
        dataSourceSelector.setValue( dataSource );
    }

    @Override
    public void loadDataSourceOptions( final List< Pair< String, String > > dataSourceOptions,
                                       final boolean addEmptyOption ) {
        clearDataSourceOptions( );
        if ( addEmptyOption ) {
            dataSourceSelector.add( newOption( "", "" ) );
        }
        for ( Pair< String, String > optionPair : dataSourceOptions ) {
            dataSourceSelector.appendChild( newOption( optionPair.getK1( ), optionPair.getK2( ) ) );
        }
    }

    @Override
    public void clearDataSourceOptions( ) {
        int length = dataSourceSelector.getLength( );
        while ( length > 0 ) {
            dataSourceSelector.remove( 0 );
            length--;
        }
    }

    private Option newOption( final String text, final String value ) {
        final Option option = ( Option ) doc.createElement( "option" );
        option.setTextContent( text );
        option.setValue( value );
        return option;
    }

    @EventHandler( "datasource-selector" )
    private void onDataSourceChange( @ForEvent( "change" ) Event event ) {
        presenter.onDataSourceChange( );
    }
}
