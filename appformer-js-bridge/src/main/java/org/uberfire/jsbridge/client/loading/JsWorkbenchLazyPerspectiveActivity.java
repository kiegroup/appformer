package org.uberfire.jsbridge.client.loading;

import java.util.function.Consumer;

import com.google.gwt.core.client.JavaScriptObject;
import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.jsbridge.JsWorkbenchLazyPerspective;
import org.uberfire.client.workbench.panels.impl.ImmutableWorkbenchPanelPresenter;
import org.uberfire.jsbridge.client.loading.AppFormerComponentConfiguration.PerspectiveComponentParams;
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

    public JsWorkbenchLazyPerspectiveActivity(final AppFormerComponentConfiguration backedComponent,
                                              final PlaceManager placeManager,
                                              final ActivityManager activityManager,
                                              final Consumer<String> lazyLoadingParentScript) {

        super(placeManager);
        this.activityManager = activityManager;

        this.backedPerspectiveId = backedComponent.getId();
        this.lazyLoadingParentScript = lazyLoadingParentScript;

        final PerspectiveComponentParams config = new PerspectiveComponentParams(backedComponent.getParams());
        this.configuredIsDefault = config.isDefault().orElse(super.isDefault());

        this.loaded = false;
    }

    @Override
    public void updateRealContent(final JavaScriptObject backedPerspective) {

        this.loaded = true;

        final JsNativePerspective jsPerspective = new JsNativePerspective(backedPerspective);
        if (jsPerspective.isTemplated()) {
            this.backedPerspective = new JsWorkbenchTemplatedPerspectiveActivity(this.getIdentifier(),
                                                                                 this.isDefault(),
                                                                                 jsPerspective,
                                                                                 placeManager);
        } else {
            this.backedPerspective = new JsWorkbenchPerspectiveActivity(jsPerspective,
                                                                        super.placeManager,
                                                                        this.isDefault());
        }

        if (this.activityManager.isStarted(this)) {
            // current activity is started, need to move the backed perspective to started state
            this.backedPerspective.onStartup(this.place);
        }

        if (this.open) {
            // lazy perspective is opened, need to move the backed perspective to open state and refresh the page
            this.backedPerspective.onOpen();
            super.placeManager.goTo(new ForcedPlaceRequest(this.backedPerspectiveId));
        }
    }

    @Override
    public PerspectiveActivity get() {
        if (this.isPerspectiveLoaded()) {
            return this.backedPerspective;
        }
        return this;
    }

    // ===== LIFECYCLE

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

        if (this.isPerspectiveLoaded()) {
            this.backedPerspective.onOpen();
        } else {
            super.onOpen();

            // trigger backed perspective loading
            this.lazyLoadingParentScript.accept(this.backedPerspectiveId);
        }

        placeManager.executeOnOpenCallbacks(place);
    }

    @Override
    public void onClose() {

        if (this.isPerspectiveLoaded()) {
            this.backedPerspective.onClose();
        } else {
            super.onClose();
        }

        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public void onShutdown() {

        if (this.isPerspectiveLoaded()) {
            this.backedPerspective.onShutdown();
            return;
        }
        super.onShutdown();
    }

    // ===== API

    @Override
    public ResourceType getResourceType() {
        return ActivityResourceType.PERSPECTIVE;
    }

    @Override
    public String getIdentifier() {
        return this.backedPerspectiveId;
    }

    @Override
    public boolean isDefault() {
        // we ignore the isDefault() property of the backed perspective,
        // it shouldn't be different than the one configured for the lazy one
        return this.configuredIsDefault;
    }

    @Override
    public boolean isTransient() {

        if (this.isPerspectiveLoaded()) {
            return this.backedPerspective.isTransient();
        }

        // lazy perspectives are always transient.
        // We don't want to propagate the changes made while the real perspective was loading.
        return true;
    }

    @Override
    public Menus getMenus() {
        if (this.isPerspectiveLoaded()) {
            return this.backedPerspective.getMenus();
        }
        return super.getMenus();
    }

    @Override
    public ToolBar getToolBar() {
        if (this.isPerspectiveLoaded()) {
            return this.backedPerspective.getToolBar();
        }
        return super.getToolBar();
    }

    @Override
    public PerspectiveDefinition getDefaultPerspectiveLayout() {

        if (this.isPerspectiveLoaded()) {
            return this.backedPerspective.getDefaultPerspectiveLayout();
        }

        return buildEmptyDefinition();
    }

    private boolean isPerspectiveLoaded() {
        return this.loaded;
    }

    private PerspectiveDefinition buildEmptyDefinition() {
        final PerspectiveDefinition def = new PerspectiveDefinitionImpl(ImmutableWorkbenchPanelPresenter.class.getName());
        def.setName(getIdentifier()); // perspective not loaded yet, we don't know its name
        def.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest(LazyLoadingScreen.IDENTIFIER)));
        return def;
    }
}
