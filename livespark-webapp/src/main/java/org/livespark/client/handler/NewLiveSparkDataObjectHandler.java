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

package org.livespark.client.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Package;
import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.screens.datamodeller.client.handlers.DomainHandler;
import org.kie.workbench.common.screens.datamodeller.client.handlers.DomainHandlerRegistry;
import org.kie.workbench.common.screens.datamodeller.client.handlers.NewJavaFileTextHandler;
import org.kie.workbench.common.screens.datamodeller.client.widgets.common.domain.ResourceOptions;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.uberfire.backend.vfs.Path;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;

import com.google.gwt.user.client.ui.Widget;

@ApplicationScoped
public class NewLiveSparkDataObjectHandler extends NewJavaFileTextHandler {

    @Inject
    private BusyIndicatorView busyIndicatorView;

    @Inject
    private Caller<DataModelerService> dataModelerService;

    @Inject
    private DomainHandlerRegistry domainHandlerRegistry;
    
    private List<ResourceOptions> resourceOptions = new ArrayList<ResourceOptions>();

    @Override
    public String getDescription() {
        // TODO make constant
        return "LiveSpark DataObject";
    }
    
    @PostConstruct
    private void setupExtensions() {

        ResourceOptions options;
        for ( DomainHandler handler : domainHandlerRegistry.getDomainHandlers() ) {
            options = handler.getResourceOptions( false );
            if ( options != null ) {
                resourceOptions.add( options );
                extensions.add( new Pair<String, Widget>( handler.getName(), options.getWidget() ) );
            }
        }
    }

    @Override
    public void create( Package pkg,
                        String baseFileName,
                        NewResourcePresenter presenter ) {
        Map<String, Object> params = new HashMap<String, Object>();
        for ( ResourceOptions options : resourceOptions ) {
            params.putAll( options.getOptions() );
        }
    	        
          
        busyIndicatorView.showBusyIndicator( CommonConstants.INSTANCE.Saving() );
        dataModelerService.call( getSuccessCallback( presenter ),
                                 new HasBusyIndicatorDefaultErrorCallback( busyIndicatorView ) )
                          .createJavaFile( idempotentAppend(pkg.getPackageMainSrcPath(), "/client/shared"),
                                           buildFileName( baseFileName,
                                                          getResourceType() ),
                                           "",
                                           params);

    }

    private Path idempotentAppend( final Path path,
                                   final String toAppend ) {
        if ( path.toURI().endsWith( toAppend ) ) return path;

        return new AppendedPath( toAppend, path );
    }

}
