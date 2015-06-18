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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Package;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.kie.workbench.common.screens.datamodeller.client.handlers.DomainOptionsHandler;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.screens.javaeditor.client.resources.JavaEditorResources;
import org.kie.workbench.common.screens.javaeditor.client.type.JavaResourceType;
import org.kie.workbench.common.widgets.client.handlers.DefaultNewResourceHandler;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.uberfire.backend.vfs.Path;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;
import org.uberfire.workbench.type.ResourceTypeDefinition;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

@ApplicationScoped
public class NewLiveSparkDataObjectHandler extends DefaultNewResourceHandler {

    @Inject
    private JavaResourceType resourceType;

    @Inject
    private BusyIndicatorView busyIndicatorView;

    @Inject
    private Caller<DataModelerService> dataModelerService;

    @Inject
    private SyncBeanManager iocBeanManager;
    
    private List<DomainOptionsHandler> optionsHandler = new ArrayList<DomainOptionsHandler>(  );

    @PostConstruct
    private void setupExtensions() {
    	 final Collection<IOCBeanDef<DomainOptionsHandler>> optionsHandlerBeans = iocBeanManager.lookupBeans( DomainOptionsHandler.class );
         if ( optionsHandlerBeans != null && optionsHandlerBeans.size() > 0 ) {
             for ( IOCBeanDef<DomainOptionsHandler> beanDef : optionsHandlerBeans ) {
                 optionsHandler.add( beanDef.getInstance() );
             }
         }
         Collections.sort( optionsHandler, new Comparator<DomainOptionsHandler>() {
             @Override public int compare( DomainOptionsHandler handler1, DomainOptionsHandler handler2 ) {
                 Integer key1 = handler1.getPriority();
                 Integer key2 = handler2.getPriority();
                 return key1.compareTo( key2 );
             }
         } );

         for ( DomainOptionsHandler handler : optionsHandler ) {
             extensions.add( new Pair<String, Widget>( handler.getName(), handler.getWidget() ) );
         }
    }

    @Override
    public String getDescription() {
        // TODO make constant
        return "LiveSpark DataObject";
    }

    @Override
    public IsWidget getIcon() {
        return new Image( JavaEditorResources.INSTANCE.images().typeJava() );
    }

    @Override
    public ResourceTypeDefinition getResourceType() {
        return resourceType;
    }

    @Override
    public void create( Package pkg,
                        String baseFileName,
                        NewResourcePresenter presenter ) {
    	  Map<String, Object> params = new HashMap<String, Object>( );
          for ( DomainOptionsHandler handler : optionsHandler ) {
              params.putAll( handler.getOptions() );
          }
          
        busyIndicatorView.showBusyIndicator( CommonConstants.INSTANCE.Saving() );
        dataModelerService.call( getSuccessCallback( presenter ),
                                 new HasBusyIndicatorDefaultErrorCallback( busyIndicatorView ) )
                          .createJavaFile( idempotentAppend(pkg.getPackageMainSrcPath(), "/client/shared"),
                                           buildFileName( baseFileName,
                                                          resourceType ),
                                           "",
                                           params);

    }

    private Path idempotentAppend( final Path path,
                                   final String toAppend ) {
        if ( path.toURI().endsWith( toAppend ) ) return path;

        return new AppendedPath( toAppend, path );
    }

}
