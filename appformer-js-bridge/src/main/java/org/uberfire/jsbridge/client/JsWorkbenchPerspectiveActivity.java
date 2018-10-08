package org.uberfire.jsbridge.client;

import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.security.ResourceType;
import org.uberfire.workbench.model.ActivityResourceType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JsWorkbenchPerspectiveActivity extends AbstractWorkbenchPerspectiveActivity {

    private JsNativePerspective realPerspective;

    public JsWorkbenchPerspectiveActivity(final JsNativePerspective realPerspective, final PlaceManager placeManager) {
        super(placeManager);
        this.realPerspective = realPerspective;
    }

    // TODO: subscriptions?

    @Override
    public void onStartup(final PlaceRequest place) {
        this.place = place;
        this.realPerspective.run("af_onStartup");
    }

    @Override
    public void onOpen() {
        //this.realPerspective.render(); TODO ??
        this.realPerspective.run("af_onOpen");
        placeManager.executeOnOpenCallbacks(place);
    }

    @Override
    public void onClose() {
        this.realPerspective.run("af_onClose");
        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public void onShutdown() {
        this.realPerspective.run("af_onShutdown");
    }

    @Override
    public ResourceType getResourceType() {
        return ActivityResourceType.PERSPECTIVE;
    }

    @Override
    public String getIdentifier() {
        return (String) realPerspective.get("af_componentId");
    }

    @Override
    public boolean isDefault() {
        return (boolean) realPerspective.get("af_isDefault");
    }

    @Override
    public boolean isTransient() {
        return (boolean) realPerspective.get("af_isTransient");
    }

    @Override
    public Menus getMenus() {
        return (Menus) realPerspective.get("af_menus");
    }

    @Override
    public ToolBar getToolBar() {
        return (ToolBar) realPerspective.get("af_toolbar");
    }

    @Override
    public PerspectiveDefinition getDefaultPerspectiveLayout() {
        final PerspectiveDefinition def = new PerspectiveDefinitionImpl();
        def.setName(getIdentifier());
        return def;
    }
}
