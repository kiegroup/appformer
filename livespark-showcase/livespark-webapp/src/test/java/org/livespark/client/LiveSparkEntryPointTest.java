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

package org.livespark.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.guvnor.common.services.shared.config.AppConfigService;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.social.hp.config.SocialConfigurationService;
import org.kie.workbench.common.services.shared.service.PlaceManagerActivityService;
import org.kie.workbench.common.workbench.client.authz.PermissionTreeSetup;
import org.kie.workbench.common.workbench.client.menu.DefaultWorkbenchFeaturesMenusHelper;
import org.livespark.client.home.HomeProducer;
import org.livespark.client.resources.i18n.AppConstants;
import org.livespark.client.shared.AppReady;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.Workbench;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBarPresenter;
import org.uberfire.ext.security.management.client.ClientUserSystemManager;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.ConstantsAnswerMock;
import org.uberfire.mocks.IocTestingUtils;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import com.google.gwtmockito.GwtMockitoTestRunner;

@RunWith(GwtMockitoTestRunner.class)
public class LiveSparkEntryPointTest {

    @Mock
    private AppConfigService appConfigService;
    private CallerMock<AppConfigService> appConfigServiceCallerMock;

    @Mock
    private PlaceManagerActivityService pmas;
    private CallerMock<PlaceManagerActivityService> pmasCallerMock;

    @Mock
    private ActivityBeansCache activityBeansCache;

    @Mock
    private HomeProducer homeProducer;

    @Mock
    private SocialConfigurationService socialConfigurationService;
    private CallerMock<SocialConfigurationService> socialConfigurationServiceCallerMock;

    @Mock
    private DefaultWorkbenchFeaturesMenusHelper menusHelper;

    @Mock
    protected ClientUserSystemManager userSystemManager;

    @Mock
    protected WorkbenchMenuBarPresenter menuBar;

    @Mock
    protected SyncBeanManager iocManager;

    @Mock
    protected Workbench workbench;

    @Mock
    private PlaceManager placeManager;
    
    @Mock
    private PermissionTreeSetup permissionTreeSetup;

    private LiveSparkEntryPoint liveSparkEntryPoint;

    @Before
    public void setup() {
        doNothing().when( pmas ).initActivities( anyList() );

        doReturn( Boolean.TRUE ).when( socialConfigurationService ).isSocialEnable();
        doAnswer( invocationOnMock -> {
            ( (Command) invocationOnMock.getArguments()[ 0 ] ).execute();
            return null;
        } ).when( userSystemManager ).waitForInitialization( any( Command.class ) );

        appConfigServiceCallerMock = new CallerMock<>( appConfigService );
        socialConfigurationServiceCallerMock = new CallerMock<>( socialConfigurationService );
        pmasCallerMock = new CallerMock<>( pmas );

        liveSparkEntryPoint = spy( new LiveSparkEntryPoint( appConfigServiceCallerMock,
                                                            pmasCallerMock,
                                                            activityBeansCache,
                                                            homeProducer,
                                                            socialConfigurationServiceCallerMock,
                                                            menusHelper,
                                                            userSystemManager,
                                                            menuBar,
                                                            iocManager,
                                                            workbench,
                                                            placeManager,
                                                            permissionTreeSetup ) );
        mockMenuHelper();
        mockConstants();
        IocTestingUtils.mockIocManager( iocManager );

        doNothing().when( liveSparkEntryPoint ).hideLoadingPopup();
    }

    @Test
    public void initTest() {
        liveSparkEntryPoint.init();

        verify( workbench ).addStartupBlocker( LiveSparkEntryPoint.class );
        verify( homeProducer ).init();
    }

    @Test
    public void onAppReady() {
        liveSparkEntryPoint.onAppReady( new AppReady( "url" ) );

        verify( placeManager ).goTo( any( PlaceRequest.class ) );
    }

    @Test
    public void setupMenuTest() {
        liveSparkEntryPoint.setupMenu();

        ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass( Menus.class );
        verify( menuBar ).addMenus( menusCaptor.capture() );

        Menus menus = menusCaptor.getValue();

        assertEquals( 6, menus.getItems().size() );

        assertEquals( liveSparkEntryPoint.constants.home(), menus.getItems().get( 0 ).getCaption() );
        assertEquals( liveSparkEntryPoint.constants.authoring(), menus.getItems().get( 1 ).getCaption() );
        assertEquals( liveSparkEntryPoint.constants.deploy(), menus.getItems().get( 2 ).getCaption() );
        assertEquals( liveSparkEntryPoint.constants.tasks(), menus.getItems().get( 3 ).getCaption() );
        assertEquals( liveSparkEntryPoint.constants.extensions(), menus.getItems().get( 4 ).getCaption() );

        verify( menusHelper ).addRolesMenuItems();
        verify( menusHelper ).addWorkbenchConfigurationMenuItem();
        verify( menusHelper ).addUtilitiesMenuItems();

        verify( workbench ).removeStartupBlocker( LiveSparkEntryPoint.class );
    }

    @Test
    public void getDeploymentViewsTest() {
        List<? extends MenuItem> deploymentMenuItems = liveSparkEntryPoint.getDeploymentViews();

        assertEquals( 1, deploymentMenuItems.size() );
        assertEquals( liveSparkEntryPoint.constants.ruleDeployments(), deploymentMenuItems.get( 0 ).getCaption() );
    }

    @Test
    public void getTasksViewTest() {
        final PlaceRequest tasksView = liveSparkEntryPoint.getTasksView();

        assertNotNull( tasksView );
    }

    private void mockMenuHelper() {
        final ArrayList<MenuItem> menuItems = new ArrayList<>();
        menuItems.add( mock( MenuItem.class ) );
        doReturn( menuItems ).when( menusHelper ).getHomeViews( anyBoolean() );
        doReturn( menuItems ).when( menusHelper ).getAuthoringViews();
        doReturn( menuItems ).when( menusHelper ).getExtensionsViews();
    }

    private void mockConstants() {
        liveSparkEntryPoint.constants = mock( AppConstants.class, new ConstantsAnswerMock() );
    }

}
