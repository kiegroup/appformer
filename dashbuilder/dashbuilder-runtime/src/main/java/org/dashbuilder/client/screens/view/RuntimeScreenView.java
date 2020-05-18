package org.dashbuilder.client.screens.view;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.dashbuilder.client.RuntimeCommunication;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.dashbuilder.client.screens.RuntimeScreen;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.client.workbench.widgets.menu.megamenu.WorkbenchMegaMenuPresenter;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@Templated
public class RuntimeScreenView implements RuntimeScreen.View {

    private static AppConstants i18n = AppConstants.INSTANCE;

    @Inject
    @DataField
    HTMLDivElement runtimePage;

    @Inject
    WorkbenchMegaMenuPresenter menuBar;

    @Inject
    RuntimeCommunication runtimeCommunication;

    @Inject
    BusyIndicatorView loading;

    @Override
    public HTMLElement getElement() {
        return runtimePage;
    }

    @Override
    public void init(RuntimeScreen presenter) {
        // empty
    }

    @Override
    public void addMenus(Menus menus) {
        menuBar.addMenus(menus);
    }

    @Override
    public void errorLoadingDashboards(Throwable throwable) {
        runtimeCommunication.showError(i18n.errorLoadingDashboards(), throwable);
    }

    @Override
    public void loading() {
        loading.showBusyIndicator(i18n.loadingDashboards());
    }

    @Override
    public void stopLoading() {
        loading.hideBusyIndicator();
    }

}