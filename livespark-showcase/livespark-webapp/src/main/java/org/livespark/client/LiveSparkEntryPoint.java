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
package org.livespark.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.RootPanel;
import org.guvnor.common.services.shared.config.AppConfigService;
import org.guvnor.common.services.shared.security.KieWorkbenchACL;
import org.guvnor.common.services.shared.security.KieWorkbenchPolicy;
import org.guvnor.common.services.shared.security.KieWorkbenchSecurityService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jbpm.console.ng.ga.forms.service.PlaceManagerActivityService;
import org.kie.workbench.common.screens.search.client.menu.SearchMenuBuilder;
import org.kie.workbench.common.screens.social.hp.config.SocialConfigurationService;
import org.kie.workbench.common.services.shared.preferences.ApplicationPreferences;
import org.kie.workbench.common.widgets.client.menu.AboutMenuBuilder;
import org.kie.workbench.common.widgets.client.menu.ResetPerspectivesMenuBuilder;
import org.kie.workbench.common.widgets.client.menu.WorkbenchConfigurationMenuBuilder;
import org.kie.workbench.common.widgets.client.resources.RoundedCornersResource;
import org.livespark.client.home.HomeProducer;
import org.livespark.client.resources.i18n.AppConstants;
import org.livespark.client.shared.AppReady;
import org.uberfire.client.menu.CustomSplashHelp;
import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.views.pfly.menu.UserMenu;
import org.uberfire.client.workbench.widgets.menu.UtilityMenuBar;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;
import org.uberfire.ext.security.management.client.ClientUserSystemManager;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.livespark.client.security.KieWorkbenchFeatures.*;

/**
 * GWT's Entry-point for livespark
 */
@EntryPoint
public class LiveSparkEntryPoint {

    private AppConstants constants = AppConstants.INSTANCE;

    @Inject
    private Caller<AppConfigService> appConfigService;

    @Inject
    private WorkbenchMenuBarPresenter menubar;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private SyncBeanManager iocManager;

    @Inject
    private ActivityManager activityManager;

    @Inject
    private User identity;

    @Inject
    private KieWorkbenchACL kieACL;

    @Inject
    private HomeProducer homeProducer;

    @Inject
    private Caller<KieWorkbenchSecurityService> kieSecurityService;

    @Inject
    private Caller<PlaceManagerActivityService> pmas;

    @Inject
    private ActivityBeansCache activityBeansCache;

    @Inject
    private Caller<AuthenticationService> authService;

    @Inject
    private Caller<SocialConfigurationService> socialConfigurationService;

    @Inject
    private UtilityMenuBar utilityMenuBar;

    @Inject
    private UserMenu userMenu;

    @Inject
    private ClientUserSystemManager userSystemManager;

    @AfterInitialization
    public void startApp() {
        kieSecurityService.call( new RemoteCallback<String>() {
            public void callback( final String str ) {
                KieWorkbenchPolicy policy = new KieWorkbenchPolicy( str );
                kieACL.activatePolicy( policy );
                loadPreferences();
                loadStyles();
                hideLoadingPopup();
                homeProducer.init();
            }
        } ).loadPolicy();
        List<String> allActivities = activityBeansCache.getActivitiesById();
        pmas.call( new RemoteCallback<Void>() {

            @Override
            public void callback( Void response ) {

            }
        } ).initActivities( allActivities );
    }

    private void onAppReady( @Observes AppReady appReady ) {
        PlaceRequest request = new DefaultPlaceRequest( "app" );
        request.addParameter( "url", appReady.getUrl() );
        placeManager.goTo( request );
    }

    private void loadPreferences() {
        appConfigService.call( new RemoteCallback<Map<String, String>>() {
            @Override
            public void callback( final Map<String, String> response ) {
                ApplicationPreferences.setUp( response );
                setupMenu();
            }
        } ).loadPreferences();
    }

    private void loadStyles() {
        //Ensure CSS has been loaded
        //ShowcaseResources.INSTANCE.showcaseCss().ensureInjected();
        RoundedCornersResource.INSTANCE.roundCornersCss().ensureInjected();
    }

    private void setupMenu() {

        // Social services.
        socialConfigurationService.call( new RemoteCallback<Boolean>() {
            public void callback( final Boolean socialEnabled ) {

                // Wait for user management services to be initialized, if any.
                userSystemManager.waitForInitialization(new Command() {

                    @Override
                    public void execute() {

                        final boolean isUserSystemManagerActive = userSystemManager.isActive();

                        final AbstractWorkbenchPerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();

                        final Menus menus =
                                MenuFactory.newTopLevelMenu(constants.home()).withItems(getHomeViews( socialEnabled, isUserSystemManagerActive )).endMenu()
                                        .newTopLevelMenu(constants.authoring()).withRoles(kieACL.getGrantedRoles(G_AUTHORING)).withItems(getAuthoringViews()).endMenu()
                                        .newTopLevelMenu(constants.deploy()).withRoles(kieACL.getGrantedRoles(G_AUTHORING)).withItems(getDeploymentViews()).endMenu()
                                        .newTopLevelMenu(constants.tasks()).place(getTasksView()).endMenu()
                                        .newTopLevelMenu(constants.extensions()).withRoles(kieACL.getGrantedRoles(F_EXTENSIONS)).withItems(getExtensionsViews()).endMenu()
                                        .newTopLevelCustomMenu(iocManager.lookupBean(SearchMenuBuilder.class).getInstance()).endMenu()
                                        .build();

                        menubar.addMenus(menus);

                        for (Menus roleMenus : getRoles()) {
                            userMenu.addMenus(roleMenus);
                        }

                        final Menus utilityMenus =
                                MenuFactory
                                        .newTopLevelCustomMenu(iocManager.lookupBean(WorkbenchConfigurationMenuBuilder.class).getInstance())
                                        .endMenu()
                                        .newTopLevelCustomMenu(iocManager.lookupBean(CustomSplashHelp.class).getInstance())
                                        .endMenu()
                                        .newTopLevelCustomMenu(iocManager.lookupBean(AboutMenuBuilder.class).getInstance())
                                        .endMenu()
                                        .newTopLevelCustomMenu(iocManager.lookupBean(ResetPerspectivesMenuBuilder.class).getInstance())
                                        .endMenu()
                                        .newTopLevelCustomMenu(userMenu)
                                        .endMenu()
                                        .build();

                        utilityMenuBar.addMenus(utilityMenus);

                    }
                });

            }
        } ).isSocialEnable();
    }

    private List<Menus> getRoles() {
        final List<Menus> result = new ArrayList<Menus>( identity.getRoles().size() );
        result.add(MenuFactory.newSimpleItem(constants.LogOut()).respondsWith(new LogoutCommand()).endMenu().build());
        for ( final Role role : identity.getRoles() ) {
            if ( !role.getName().equals( "IS_REMEMBER_ME" ) ) {
                result.add( MenuFactory.newSimpleItem( constants.Role() + ": " + role.getName() ).endMenu().build() );
            }
        }
        return result;
    }

    private List<? extends MenuItem> getHomeViews( final Boolean socialEnabled, final boolean usersSystemActive  ) {
        final AbstractWorkbenchPerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();
        final List<MenuItem> result = new ArrayList<MenuItem>( 1 );

        result.add( MenuFactory.newSimpleItem( constants.homePage() ).place( new DefaultPlaceRequest( defaultPerspective.getIdentifier() ) ).endMenu().build().getItems().get( 0 ) );

        // Social menu items.
        if ( socialEnabled) {
            result.add( MenuFactory.newSimpleItem( constants.timeline() ).place( new DefaultPlaceRequest( "SocialHomePagePerspective" ) ).endMenu().build().getItems().get(0));

            result.add( MenuFactory.newSimpleItem( constants.people() ).place( new DefaultPlaceRequest( "UserHomePagePerspective" ) ).endMenu().build().getItems().get( 0 ) );
        }

        // User management menu items (only if services are active and constrained to admin roles).
        if ( usersSystemActive ) {
            result.add( MenuFactory.newSimpleItem( constants.userManagement()).withRoles(kieACL.getGrantedRoles( F_ADMINISTRATION ) ).place( new DefaultPlaceRequest( "UsersManagementPerspective" ) ).endMenu().build().getItems().get( 0 ) );
            result.add( MenuFactory.newSimpleItem( constants.groupManagement()).withRoles(kieACL.getGrantedRoles( F_ADMINISTRATION ) ).place( new DefaultPlaceRequest( "GroupsManagementPerspective" ) ).endMenu().build().getItems().get( 0 ) );
        }

        return result;
    }

    private List<MenuItem> getAuthoringViews() {
        final List<MenuItem> result = new ArrayList<MenuItem>( 4 );

        result.add( MenuFactory.newSimpleItem( constants.project_authoring() ).withRoles( kieACL.getGrantedRoles( F_PROJECT_AUTHORING ) ).place( new DefaultPlaceRequest( "AuthoringPerspective" ) ).endMenu().build().getItems().get( 0 ) );

        result.add( MenuFactory.newSimpleItem( constants.contributors() ).withRoles( kieACL.getGrantedRoles( F_CONTRIBUTORS ) ).place( new DefaultPlaceRequest( "ContributorsPerspective" ) ).endMenu().build().getItems().get( 0 ) );

        result.add( MenuFactory.newSimpleItem( constants.artifactRepository() ).withRoles( kieACL.getGrantedRoles( F_ARTIFACT_REPO ) ).place( new DefaultPlaceRequest( "org.guvnor.m2repo.client.perspectives.GuvnorM2RepoPerspective" ) ).endMenu().build().getItems().get( 0 ) );

        result.add(MenuFactory.newSimpleItem(constants.administration()).withRoles(kieACL.getGrantedRoles(F_ADMINISTRATION)).place(new DefaultPlaceRequest("AdministrationPerspective")).endMenu().build().getItems().get(0));

        return result;
    }

    private List<MenuItem> getDeploymentViews() {
        final List<MenuItem> result = new ArrayList<MenuItem>( 1 );

        result.add( MenuFactory.newSimpleItem( constants.ruleDeployments() ).withRoles( kieACL.getGrantedRoles( F_MANAGEMENT ) ).place( new DefaultPlaceRequest( "ServerManagementPerspective" ) ).endMenu().build().getItems().get( 0 ) );

        return result;
    }

    private PlaceRequest getTasksView() {
        return new DefaultPlaceRequest( "DataSet Tasks" );
    }

    private List<? extends MenuItem> getExtensionsViews() {
        final List<MenuItem> result = new ArrayList<MenuItem>( 2 );
        result.add( MenuFactory.newSimpleItem( constants.plugins() ).withRoles( kieACL.getGrantedRoles( F_PLUGIN_MANAGEMENT ) ).place( new DefaultPlaceRequest( "PlugInAuthoringPerspective" ) ).endMenu().build().getItems().get( 0 ) );
        result.add( MenuFactory.newSimpleItem( constants.Apps() ).withRoles( kieACL.getGrantedRoles( F_APPS ) ).place( new DefaultPlaceRequest( "AppsPerspective" ) ).endMenu().build().getItems().get( 0 ) );
        result.add( MenuFactory.newSimpleItem( constants.DataSets() ).withRoles( kieACL.getGrantedRoles( F_DATASETS ) ).place( new DefaultPlaceRequest( "DataSetAuthoringPerspective" ) ).endMenu().build().getItems().get( 0 ) );
        return result;
    }

    private AbstractWorkbenchPerspectiveActivity getDefaultPerspectiveActivity() {
        AbstractWorkbenchPerspectiveActivity defaultPerspective = null;
        final Collection<SyncBeanDef<AbstractWorkbenchPerspectiveActivity>> perspectives = iocManager.lookupBeans( AbstractWorkbenchPerspectiveActivity.class );
        final Iterator<SyncBeanDef<AbstractWorkbenchPerspectiveActivity>> perspectivesIterator = perspectives.iterator();
        outer_loop:
        while ( perspectivesIterator.hasNext() ) {
            final SyncBeanDef<AbstractWorkbenchPerspectiveActivity> perspective = perspectivesIterator.next();
            final AbstractWorkbenchPerspectiveActivity instance = perspective.getInstance();
            if ( instance.isDefault() ) {
                defaultPerspective = instance;
                break outer_loop;
            } else {
                iocManager.destroyBean( instance );
            }
        }
        return defaultPerspective;
    }

    //Fade out the "Loading application" pop-up
    private void hideLoadingPopup() {
        final Element e = RootPanel.get( "loading" ).getElement();

        new Animation() {

            @Override
            protected void onUpdate( double progress ) {
                e.getStyle().setOpacity( 1.0 - progress );
            }

            @Override
            protected void onComplete() {
                e.getStyle().setVisibility( Style.Visibility.HIDDEN );
            }
        }.run( 500 );
    }

    private class LogoutCommand implements Command {

        @Override
        public void execute() {
            authService.call( new RemoteCallback<Void>() {
                @Override
                public void callback( Void response ) {
                    final String location = GWT.getModuleBaseURL().replaceFirst( "/" + GWT.getModuleName() + "/", "/logout.jsp" );
                    redirect( location );
                }
            } ).logout();
        }
    }

    public static native void redirect( String url )/*-{
        $wnd.location = url;
    }-*/;

}
