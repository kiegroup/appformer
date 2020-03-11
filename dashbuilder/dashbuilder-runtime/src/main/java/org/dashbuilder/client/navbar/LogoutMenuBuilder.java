package org.dashbuilder.client.navbar;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.IsWidget;
import elemental2.dom.DomGlobal;
import org.dashbuilder.client.resources.i18n.AppConstants;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuFactory.CustomMenuBuilder;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.MenuPosition;
import org.uberfire.workbench.model.menu.impl.BaseMenuCustom;

@ApplicationScoped
public class LogoutMenuBuilder implements MenuFactory.CustomMenuBuilder {

    private AnchorListItem link = new AnchorListItem();

    @PostConstruct
    public void buildLink() {
        link.setIcon(IconType.SIGN_OUT);

        link.getWidget(0).setStyleName("nav-item-iconic"); // Fix for IE11
        link.setTitle(AppConstants.INSTANCE.logoutMenuTooltip());
        link.addClickHandler(e -> DomGlobal.window.location.assign("/rest/logout"));

    }

    @Override
    public void push(CustomMenuBuilder element) {
        // do nothing
    }

    @Override
    public MenuItem build() {
        return new BaseMenuCustom<IsWidget>() {

            @Override
            public IsWidget build() {
                return link;
            }

            @Override
            public MenuPosition getPosition() {
                return MenuPosition.RIGHT;
            }
        };

    }

}