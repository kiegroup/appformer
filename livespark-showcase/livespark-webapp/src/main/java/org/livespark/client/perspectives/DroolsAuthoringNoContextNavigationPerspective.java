/*
 * Copyright 2016 JBoss Inc
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
package org.livespark.client.perspectives;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.widgets.client.handlers.NewResourcesMenu;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.PanelManager;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PartDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@ApplicationScoped
@WorkbenchPerspective(identifier = "AuthoringPerspectiveNoContext", isTransient = false)
public class DroolsAuthoringNoContextNavigationPerspective {

    private org.livespark.client.resources.i18n.AppConstants constants = org.livespark.client.resources.i18n.AppConstants.INSTANCE;

    @Inject
    private NewResourcesMenu newResourcesMenu;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private PanelManager panelManager;

    @Inject
    private Caller<VFSService> vfsServices;

    @Inject
    private org.livespark.client.docks.AuthoringWorkbenchDocks docks;

    private String explorerMode;
    private String projectPathString;
    private boolean projectEditorDisableBuild;

    private final List<PlaceRequest> placesToClose = new ArrayList<PlaceRequest>();

    @PostConstruct
    public void init() {
        explorerMode = ( ( Window.Location.getParameterMap().containsKey( "explorer_mode" ) ) ? Window.Location.getParameterMap().get( "explorer_mode" ).get( 0 ) : "" ).trim();
        projectPathString = ( ( ( Window.Location.getParameterMap().containsKey( "path" ) ) ? Window.Location.getParameterMap().get( "path" ).get( 0 ) : "" ) ).trim();
        projectEditorDisableBuild = Window.Location.getParameterMap().containsKey("no_build");

        final PlaceRequest placeRequest = generateProjectExplorerPlaceRequest();

        docks.setup("AuthoringPerspectiveNoContext", placeRequest);

    }

    private PlaceRequest generateProjectExplorerPlaceRequest() {
        final PlaceRequest placeRequest = new DefaultPlaceRequest( "org.kie.guvnor.explorer" );
        if ( !explorerMode.isEmpty() ) {
            placeRequest.addParameter( "mode",
                    explorerMode );
        }
        if ( !projectPathString.isEmpty() ) {
            placeRequest.addParameter( "init_path",
                    projectPathString );
        }
        if ( projectEditorDisableBuild ) {
            placeRequest.addParameter( "no_build",
                    "true" );
        }

        placeRequest.addParameter("no_context",
                "true");
        return placeRequest;
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinitionImpl perspective = new PerspectiveDefinitionImpl( MultiListWorkbenchPanelPresenter.class.getName() );
        perspective.setName( constants.project_authoring() );

        return perspective;
    }

    @OnOpen
    public void onOpen() {
        placesToClose.clear();
        if ( !projectPathString.isEmpty() ) {
            vfsServices.call( new RemoteCallback<Boolean>() {
                @Override
                public void callback( Boolean isRegularFile ) {
                    if ( isRegularFile ) {
                        vfsServices.call( new RemoteCallback<Path>() {
                            @Override
                            public void callback( Path path ) {
                                placeManager.goTo( path );
                            }
                        } ).get( projectPathString );
                    }
                }
            } ).isRegularFile( projectPathString );
        }
        if ( panelManager.getRoot() != null ) {
            process( panelManager.getRoot().getParts() );
            process( panelManager.getRoot().getChildren() );

            for ( final PlaceRequest placeRequest : placesToClose ) {
                placeManager.forceClosePlace( placeRequest );
            }
        }
    }

    private void process( final List<PanelDefinition> children ) {
        for ( final PanelDefinition child : children ) {
            process( child.getParts() );
            process( child.getChildren() );
        }
    }

    private void process( final Collection<PartDefinition> parts ) {
        for ( final PartDefinition partDefinition : parts ) {
            if ( !partDefinition.getPlace().getIdentifier().equals( "org.kie.guvnor.explorer" ) ) {
                placesToClose.add( partDefinition.getPlace() );
            }
        }
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return MenuFactory.newTopLevelMenu( constants.newItem() )
                .withItems( newResourcesMenu.getMenuItemsWithoutProject() ).endMenu()
                .build();
    }
}
