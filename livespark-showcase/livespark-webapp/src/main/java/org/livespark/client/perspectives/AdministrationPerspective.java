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

import org.guvnor.asset.management.client.editors.repository.wizard.CreateRepositoryWizard;
import org.guvnor.common.services.shared.security.KieWorkbenchACL;
import org.guvnor.structure.client.editors.repository.clone.CloneRepositoryPresenter;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.client.workbench.panels.impl.SimpleWorkbenchPanelPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.security.annotations.Roles;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import static org.livespark.client.security.KieWorkbenchFeatures.*;

/**
 * A Perspective for Administrators
 */
@Roles({ "admin" })
@ApplicationScoped
@WorkbenchPerspective(identifier = "AdministrationPerspective")
public class AdministrationPerspective {

    private org.livespark.client.resources.i18n.AppConstants constants = org.livespark.client.resources.i18n.AppConstants.INSTANCE;

    @Inject
    private NewResourcePresenter newResourcePresenter;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private SyncBeanManager iocManager;

    @Inject
    private KieWorkbenchACL kieACL;

    @Inject
    private CloneRepositoryPresenter cloneRepositoryPresenter;

    private Command newRepoCommand = null;
    private Command cloneRepoCommand = null;

    @PostConstruct
    public void init() {
        buildCommands();
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( MultiListWorkbenchPanelPresenter.class.getName() );
        perspective.setName( constants.administration() );

        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "RepositoriesEditor" ) ) );

        final PanelDefinition west = new PanelDefinitionImpl( SimpleWorkbenchPanelPresenter.class.getName() );
        west.setWidth( 300 );
        west.setMinWidth( 200 );
        west.addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "FileExplorer" ) ) );

        perspective.getRoot().insertChild( CompassPosition.WEST, west );

        return perspective;
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return MenuFactory
                .newTopLevelMenu( constants.MenuOrganizationalUnits() )
                .withRoles( kieACL.getGrantedRoles( F_ADMINISTRATION ) )
                .menus()
                .menu( constants.MenuManageOrganizationalUnits() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "org.kie.workbench.common.screens.organizationalunit.manager.OrganizationalUnitManager" );
                    }
                } )
                .endMenu()
                .endMenus()
                .endMenu()
                .newTopLevelMenu( constants.repositories() )
                .withRoles( kieACL.getGrantedRoles( F_ADMINISTRATION ) )
                .menus()
                .menu( constants.listRepositories() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        placeManager.goTo( "RepositoriesEditor" );
                    }
                } )
                .endMenu()
                .menu( constants.cloneRepository() )
                .respondsWith( cloneRepoCommand )
                .endMenu()
                .menu( constants.newRepository() )
                .respondsWith( newRepoCommand )
                .endMenu()
                .endMenus()
                .endMenu().build();
    }

    private void buildCommands() {
        this.cloneRepoCommand = new Command() {

            @Override
            public void execute() {
                cloneRepositoryPresenter.showForm();
            }

        };

        this.newRepoCommand = new Command() {
            @Override
            public void execute() {
                final CreateRepositoryWizard newRepositoryWizard = iocManager.lookupBean( CreateRepositoryWizard.class ).getInstance();
                //When pop-up is closed destroy bean to avoid memory leak
                newRepositoryWizard.onCloseCallback( new Callback<Void>() {
                    @Override
                    public void callback( Void result ) {
                        iocManager.destroyBean( newRepositoryWizard );
                    }
                } );
                newRepositoryWizard.start();
            }
        };
    }
}
