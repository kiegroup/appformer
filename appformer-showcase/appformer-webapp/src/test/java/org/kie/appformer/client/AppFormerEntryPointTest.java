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

package org.kie.appformer.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.common.services.shared.config.AppConfigService;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.client.resources.i18n.AppConstants;
import org.kie.appformer.provisioning.shared.AppReady;
import org.kie.workbench.common.screens.social.hp.config.SocialConfigurationService;
import org.kie.workbench.common.workbench.client.authz.PermissionTreeSetup;
import org.kie.workbench.common.workbench.client.menu.DefaultWorkbenchFeaturesMenusHelper;
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class AppFormerEntryPointTest {

    @Mock
    private AppConfigService appConfigService;
    private CallerMock<AppConfigService> appConfigServiceCallerMock;

    @Mock
    private ActivityBeansCache activityBeansCache;

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

    private AppFormerEntryPoint entryPoint;

    @Before
    public void setup() {
        doReturn(Boolean.TRUE).when(socialConfigurationService).isSocialEnable();
        doAnswer(invocationOnMock -> {
            ((Command) invocationOnMock.getArguments()[0]).execute();
            return null;
        }).when(userSystemManager).waitForInitialization(any(Command.class));

        appConfigServiceCallerMock = new CallerMock<>(appConfigService);
        socialConfigurationServiceCallerMock = new CallerMock<>(socialConfigurationService);

        entryPoint = spy(new AppFormerEntryPoint(appConfigServiceCallerMock,
                                                 activityBeansCache,
                                                 socialConfigurationServiceCallerMock,
                                                 menusHelper,
                                                 userSystemManager,
                                                 menuBar,
                                                 iocManager,
                                                 workbench,
                                                 placeManager,
                                                 permissionTreeSetup));
        mockMenuHelper();
        mockConstants();
        IocTestingUtils.mockIocManager(iocManager);

        doNothing().when(entryPoint).hideLoadingPopup();
    }

    @Test
    public void initTest() {
        entryPoint.init();

        verify(workbench).addStartupBlocker(AppFormerEntryPoint.class);
    }

    @Test
    public void onAppReady() {
        entryPoint.onAppReady(new AppReady("url"));

        verify(placeManager).goTo(any(PlaceRequest.class));
    }

    @Test
    public void setupMenuTest() {
        entryPoint.setupMenu();

        final ArgumentCaptor<Menus> menusCaptor = ArgumentCaptor.forClass(Menus.class);
        verify(menuBar).addMenus(menusCaptor.capture());

        final Menus menus = menusCaptor.getValue();

        assertEquals(5,
                     menus.getItems().size());

        assertEquals(entryPoint.constants.home(),
                     menus.getItems().get(0).getCaption());
        assertEquals(entryPoint.constants.authoring(),
                     menus.getItems().get(1).getCaption());
        assertEquals(entryPoint.constants.deploy(),
                     menus.getItems().get(2).getCaption());
        assertEquals(entryPoint.constants.extensions(),
                     menus.getItems().get(3).getCaption());

        verify(menusHelper).addRolesMenuItems();
        verify(menusHelper).addWorkbenchConfigurationMenuItem();
        verify(menusHelper).addUtilitiesMenuItems();

        verify(workbench).removeStartupBlocker(AppFormerEntryPoint.class);
    }

    @Test
    public void getDeploymentViewsTest() {
        final List<? extends MenuItem> deploymentMenuItems = entryPoint.getDeploymentViews();

        assertEquals(1,
                     deploymentMenuItems.size());
        assertEquals(entryPoint.constants.ruleDeployments(),
                     deploymentMenuItems.get(0).getCaption());
    }

    private void mockMenuHelper() {
        final ArrayList<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(mock(MenuItem.class));
        doReturn(menuItems).when(menusHelper).getHomeViews(anyBoolean());
        doReturn(menuItems).when(menusHelper).getAuthoringViews();
        doReturn(menuItems).when(menusHelper).getExtensionsViews();
    }

    private void mockConstants() {
        entryPoint.constants = mock(AppConstants.class,
                                    new ConstantsAnswerMock());
    }
}
