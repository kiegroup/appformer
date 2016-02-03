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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.inbox.client.InboxPresenter;
import org.kie.workbench.common.screens.examples.client.wizard.ExamplesWizard;
import org.kie.workbench.common.screens.examples.service.ExamplesService;
import org.kie.workbench.common.services.shared.preferences.ApplicationPreferences;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.kie.workbench.common.widgets.client.handlers.NewResourcesMenu;
import org.kie.workbench.common.widgets.client.menu.RepositoryMenu;
import org.livespark.client.docks.AuthoringWorkbenchDocks;
import org.livespark.client.resources.i18n.AppConstants;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuPosition;
import org.uberfire.workbench.model.menu.Menus;

/**
 * A Perspective for Rule authors
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "AuthoringPerspective", isTransient = false)
public class DroolsAuthoringPerspective {

    private AppConstants constants = AppConstants.INSTANCE;

    @Inject
    private NewResourcePresenter newResourcePresenter;

    @Inject
    private NewResourcesMenu newResourcesMenu;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private RepositoryMenu repositoryMenu;

    @Inject
    private AuthoringWorkbenchDocks docks;

    @Inject
    private ExamplesWizard wizard;

    @PostConstruct
    public void setup() {
        docks.setup( "AuthoringPerspective", new DefaultPlaceRequest( "org.kie.guvnor.explorer" ) );
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( MultiListWorkbenchPanelPresenter.class.getName() );
        perspective.setName( constants.project_authoring() );

        final PanelDefinition south = new PanelDefinitionImpl( MultiListWorkbenchPanelPresenter.class.getName() );
        south.setWidth( 400 );
        south.setMinWidth( 350 );
        south.setHeight(300);
        south.addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "MavenBuildOutput" ) ) );
        perspective.getRoot().insertChild( CompassPosition.SOUTH,
                south);
        return perspective;
    }

    @WorkbenchMenu
    public Menus getMenus() {
        if ( !ApplicationPreferences.isProductized() && ApplicationPreferences.getBooleanPref( ExamplesService.EXAMPLES_SYSTEM_PROPERTY ) ) {
            return buildMenuBarWithExamples();

        } else {
            return buildMenuBarWithoutExamples();
        }
    }

    private Menus buildMenuBarWithExamples() {
        return MenuFactory
                .newTopLevelMenu( constants.Examples() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        wizard.start();
                    }
                } )
                .endMenu()
                .newTopLevelMenu( constants.explore() )
                .menus()
                .menu( constants.inboxIncomingChanges() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "Inbox" );
                    }
                } )
                .endMenu()
                .menu( constants.inboxRecentlyEdited() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        PlaceRequest p = new DefaultPlaceRequest( "Inbox" );
                        p.addParameter( "inboxname", InboxPresenter.RECENT_EDITED_ID );
                        placeManager.goTo( p );
                    }
                } )
                .endMenu()
                .menu( constants.inboxRecentlyOpened() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        PlaceRequest p = new DefaultPlaceRequest( "Inbox" );
                        p.addParameter( "inboxname", InboxPresenter.RECENT_VIEWED_ID );
                        placeManager.goTo( p );
                    }
                } )
                .endMenu()
                .endMenus()
                .endMenu()
                .newTopLevelMenu( constants.newItem() )
                .withItems( newResourcesMenu.getMenuItems() )
                .endMenu()
                .newTopLevelMenu( constants.Repository() )
                .withItems( repositoryMenu.getMenuItems() )
                .endMenu()
                .newTopLevelMenu( constants.assetSearch() ).position( MenuPosition.RIGHT ).respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "FindForm" );
                    }
                } )
                .endMenu()
                .newTopLevelMenu( constants.Messages() ).position( MenuPosition.RIGHT ).respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "org.kie.workbench.common.screens.messageconsole.MessageConsole" );
                    }
                } )
                .endMenu()
                .build();
    }

    private Menus buildMenuBarWithoutExamples() {
        return MenuFactory
                .newTopLevelMenu( constants.explore() )
                .menus()
                .menu( constants.inboxIncomingChanges() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "Inbox" );
                    }
                } )
                .endMenu()
                .menu( constants.inboxRecentlyEdited() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        PlaceRequest p = new DefaultPlaceRequest( "Inbox" );
                        p.addParameter( "inboxname", InboxPresenter.RECENT_EDITED_ID );
                        placeManager.goTo( p );
                    }
                } )
                .endMenu()
                .menu( constants.inboxRecentlyOpened() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        PlaceRequest p = new DefaultPlaceRequest( "Inbox" );
                        p.addParameter( "inboxname", InboxPresenter.RECENT_VIEWED_ID );
                        placeManager.goTo( p );
                    }
                } )
                .endMenu()
                .endMenus()
                .endMenu()
                .newTopLevelMenu( constants.newItem() )
                .withItems( newResourcesMenu.getMenuItems() )
                .endMenu()
                .newTopLevelMenu( constants.Repository() )
                .withItems( repositoryMenu.getMenuItems() )
                .endMenu()
                .newTopLevelMenu( constants.assetSearch() ).position( MenuPosition.RIGHT ).respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "FindForm" );
                    }
                } )
                .endMenu()
                .newTopLevelMenu( constants.Messages() ).position( MenuPosition.RIGHT ).respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "org.kie.workbench.common.screens.messageconsole.MessageConsole" );
                    }
                } )
                .endMenu()
                .build();
    }

}
