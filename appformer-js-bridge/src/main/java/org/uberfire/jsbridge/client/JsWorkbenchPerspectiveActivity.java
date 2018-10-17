package org.uberfire.jsbridge.client;

import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.security.ResourceType;
import org.uberfire.workbench.model.ActivityResourceType;
import org.uberfire.workbench.model.ContextDisplayMode;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.ContextDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

import static java.util.stream.Collectors.toList;

public class JsWorkbenchPerspectiveActivity extends AbstractWorkbenchPerspectiveActivity {

    private JsNativePerspective realPerspective;

    public JsWorkbenchPerspectiveActivity(final JsNativePerspective realPerspective, final PlaceManager placeManager) {
        super(placeManager);
        this.realPerspective = realPerspective;
    }

    // TODO: subscriptions?

    /**
     * This method is called when this perspective is instantiated
     * @param place
     */
    @Override
    public void onStartup(final PlaceRequest place) {
        super.onStartup(place);

        this.place = place;
        this.realPerspective.run("af_onStartup");
    }

    @Override
    public void onOpen() {
        super.onOpen();

        //this.realPerspective.render(); TODO ??
        this.realPerspective.run("af_onOpen");
        placeManager.executeOnOpenCallbacks(place);
    }

    @Override
    public void onClose() {
        super.onClose();

        this.realPerspective.run("af_onClose");
        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();

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

        final PerspectiveDefinition def = new PerspectiveDefinitionImpl(defaultPanelType());
        def.setName(getIdentifier());

        final PanelDefinition rootPanel = def.getRoot();

        final String contextId = this.realPerspective.contextId();
        if (contextId != null) {
            def.setContextDefinition(new ContextDefinitionImpl(new DefaultPlaceRequest(contextId)));
        }

        final ContextDisplayMode contextDisplayMode = this.realPerspective.contextDisplayMode();
        def.setContextDisplayMode(contextDisplayMode);

        realPerspective.parts().stream()
                .map(part -> new JsWorkbenchPartConverter(part).toPartDefinition())
                .collect(toList())
                .forEach(rootPanel::addPart);

        realPerspective.panels().stream()
                .map(panel -> new JsWorkbenchPanelConverter(panel).toPanelDefinition())
                .collect(toList())
                .forEach(panel -> rootPanel.insertChild(panel.getPosition(), panel));

        return def;
    }

    private String defaultPanelType() {
        return (String) realPerspective.get("af_defaultPanelType");
    }
}
