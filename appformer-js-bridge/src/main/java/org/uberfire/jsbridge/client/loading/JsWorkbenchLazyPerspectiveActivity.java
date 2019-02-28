package org.uberfire.jsbridge.client.loading;

import java.util.function.Consumer;

import com.google.gwt.core.client.JavaScriptObject;
import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.jsbridge.JsWorkbenchLazyPerspective;
import org.uberfire.client.workbench.panels.impl.ImmutableWorkbenchPanelPresenter;
import org.uberfire.jsbridge.client.perspective.JsWorkbenchPerspectiveActivity;
import org.uberfire.jsbridge.client.perspective.JsWorkbenchTemplatedPerspectiveActivity;
import org.uberfire.jsbridge.client.perspective.jsnative.JsNativePerspective;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.mvp.impl.ForcedPlaceRequest;
import org.uberfire.security.ResourceType;
import org.uberfire.workbench.model.ActivityResourceType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JsWorkbenchLazyPerspectiveActivity extends AbstractWorkbenchPerspectiveActivity implements JsWorkbenchLazyPerspective {

    private final ActivityManager activityManager;

    private final String backedPerspectiveId;
    private final boolean configuredIsDefault;
    private PerspectiveActivity backedPerspective;

    private boolean loaded;
    private final Consumer<String> lazyLoadingParentScript;

    public JsWorkbenchLazyPerspectiveActivity(final AppFormerComponentsRegistry.Entry registryEntry,
                                              final PlaceManager placeManager,
                                              final ActivityManager activityManager,
                                              final Consumer<String> lazyLoadingParentScript) {

        super(placeManager);
        this.activityManager = activityManager;

        this.backedPerspectiveId = registryEntry.getComponentId();
        this.lazyLoadingParentScript = lazyLoadingParentScript;

        this.configuredIsDefault = new AppFormerComponentsRegistry.Entry.PerspectiveParams(registryEntry.getParams())
                .isDefault().orElse(super.isDefault());

        this.loaded = false;
    }

    @Override
    public void updateRealContent(final JavaScriptObject backedPerspectiveJsObject) {

        this.loaded = true;

        final JsNativePerspective jsPerspective = new JsNativePerspective(backedPerspectiveJsObject);
        if (jsPerspective.isTemplated()) {
            backedPerspective = new JsWorkbenchTemplatedPerspectiveActivity(getIdentifier(),
                                                                            isDefault(),
                                                                            jsPerspective,
                                                                            placeManager);
        } else {
            backedPerspective = new JsWorkbenchPerspectiveActivity(jsPerspective,
                                                                   placeManager,
                                                                   isDefault());
        }

        if (activityManager.isStarted(this)) {
            // current activity is started, need to move the backed perspective to started state
            backedPerspective.onStartup(place);
        }

        if (open) {
            // lazy perspective is opened, need to move the backed perspective to open state and refresh the page
            backedPerspective.onOpen();
            placeManager.goTo(new ForcedPlaceRequest(backedPerspectiveId));
        }
    }

    @Override
    public PerspectiveActivity get() {
        if (isPerspectiveLoaded()) {
            return backedPerspective;
        }
        return this;
    }

    // Lifecycle

    @Override
    public void onStartup(final PlaceRequest place) {

        this.place = place;

        if (isPerspectiveLoaded()) {
            this.backedPerspective.onStartup(place);
            return;
        }
        super.onStartup(place);
    }

    @Override
    public void onOpen() {

        if (isPerspectiveLoaded()) {
            backedPerspective.onOpen();
        } else {
            super.onOpen();

            // trigger backed perspective loading
            lazyLoadingParentScript.accept(backedPerspectiveId);
        }

        placeManager.executeOnOpenCallbacks(place);
    }

    @Override
    public void onClose() {

        if (isPerspectiveLoaded()) {
            backedPerspective.onClose();
        } else {
            super.onClose();
        }

        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public void onShutdown() {

        if (isPerspectiveLoaded()) {
            backedPerspective.onShutdown();
            return;
        }
        super.onShutdown();
    }

    // Properties

    @Override
    public ResourceType getResourceType() {
        return ActivityResourceType.PERSPECTIVE;
    }

    @Override
    public String getIdentifier() {
        return backedPerspectiveId;
    }

    @Override
    public boolean isDefault() {
        // we ignore the isDefault() property of the backed perspective,
        // it shouldn't be different than the one configured for the lazy one
        return configuredIsDefault;
    }

    @Override
    public boolean isTransient() {

        if (isPerspectiveLoaded()) {
            return backedPerspective.isTransient();
        }

        // lazy perspectives are always transient.
        // We don't want to propagate the changes made while the real perspective was loading.
        return true;
    }

    @Override
    public Menus getMenus() {
        if (isPerspectiveLoaded()) {
            return backedPerspective.getMenus();
        }
        return super.getMenus();
    }

    @Override
    public ToolBar getToolBar() {
        if (isPerspectiveLoaded()) {
            return backedPerspective.getToolBar();
        }
        return super.getToolBar();
    }

    @Override
    public PerspectiveDefinition getDefaultPerspectiveLayout() {

        if (isPerspectiveLoaded()) {
            return backedPerspective.getDefaultPerspectiveLayout();
        }

        return buildEmptyDefinition();
    }

    private boolean isPerspectiveLoaded() {
        return loaded;
    }

    private PerspectiveDefinition buildEmptyDefinition() {
        final PerspectiveDefinition def = new PerspectiveDefinitionImpl(ImmutableWorkbenchPanelPresenter.class.getName());
        def.setName(getIdentifier()); // perspective not loaded yet, we don't know its name
        def.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest(LazyLoadingScreen.IDENTIFIER)));
        return def;
    }
}
