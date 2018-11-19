package org.uberfire.jsbridge.client.loading;

import javax.annotation.PostConstruct;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.workbench.model.menu.Menus;

@Templated("lazy-loading.html")
@WorkbenchScreen(identifier = LazyLoadingScreen.IDENTIFIER)
public class LazyLoadingScreen extends Composite {

    public static final String IDENTIFIER = "LazyLoadingScreen";

    private Label title;

    @PostConstruct
    public void init() {
        this.title = new Label(getTitle());
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Lazy Loading Screen";
    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitleDecoration() {
        return title;
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return this;
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return null;
    }
}
