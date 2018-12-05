package org.uberfire.jsbridge.client.perspective;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import elemental2.dom.Attr;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.NamedNodeMap;
import elemental2.dom.Node;
import jsinterop.base.Js;
import org.uberfire.client.mvp.AbstractWorkbenchPerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.TemplatedActivity;
import org.uberfire.client.workbench.panels.impl.TemplatedWorkbenchPanelPresenter;
import org.uberfire.commons.data.Pair;
import org.uberfire.jsbridge.client.perspective.jsnative.JsNativePerspective;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.security.ResourceType;
import org.uberfire.workbench.model.ActivityResourceType;
import org.uberfire.workbench.model.NamedPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

import static java.util.stream.Collectors.toMap;
import static org.uberfire.commons.data.Pair.newPair;
import static org.uberfire.workbench.model.PanelDefinition.PARENT_CHOOSES_TYPE;

public class JsWorkbenchTemplatedPerspectiveActivity extends AbstractWorkbenchPerspectiveActivity implements TemplatedActivity {

    private static final String UF_PERSPECTIVE_COMPONENT = "uf-perspective-component";
    private static final String UF_PERSPECTIVE_CONTAINER = "uf-perspective-container";
    private static final String STARTUP_PARAM_ATTR = "data-startup-";
    private static final String AF_JS_COMPONENT = "af-js-component";

    private final String componentId;
    private final boolean isDefault;

    private final HTMLElement container;

    private final JsNativePerspective realPerspective;
    private Map<String, HTMLElement> componentContainersById;

    public JsWorkbenchTemplatedPerspectiveActivity(final String componentId,
                                                   final boolean isDefault,
                                                   final JsNativePerspective realPerspective,
                                                   final PlaceManager placeManager) {
        super(placeManager);

        this.componentId = componentId;
        this.isDefault = isDefault;

        this.realPerspective = realPerspective;

        this.container = (HTMLElement) DomGlobal.document.createElement("div");
        this.container.classList.add(UF_PERSPECTIVE_CONTAINER);
        this.componentContainersById = new HashMap<>();
    }

    // ====== Perspective lifecycle
    @Override
    public void onStartup(final PlaceRequest place) {
        this.place = place;
        super.onStartup(place);
        this.realPerspective.onStartup();
    }

    @Override
    public void onOpen() {
        super.onOpen();

        // update local references to the DOM elements
        this.realPerspective.renderNative(this.container);
        this.componentContainersById = loadTemplateComponents(this.container);

        this.realPerspective.onOpen();
        placeManager.executeOnOpenCallbacks(place);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.realPerspective.onClose(this.container);
        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        this.realPerspective.onShutdown();
    }
    // ======

    // ====== Perspective methods
    @Override
    public ResourceType getResourceType() {
        return ActivityResourceType.PERSPECTIVE;
    }

    @Override
    public String getIdentifier() {
        return this.componentId;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public boolean isTransient() {
        return realPerspective.isTransient();
    }

    @Override
    public Menus getMenus() {
        return realPerspective.menus();
    }

    @Override
    public ToolBar getToolBar() {
        return realPerspective.toolbar();
    }

    @Override
    public PerspectiveDefinition getDefaultPerspectiveLayout() {
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl(TemplatedWorkbenchPanelPresenter.class.getName());
        p.setName(realPerspective.name());

        this.componentContainersById.forEach((key, value) -> {

            final Map<String, String> placeParams = retrieveStartUpParams(value);
            final PanelDefinition panelDefinition2 = new PanelDefinitionImpl(PARENT_CHOOSES_TYPE);

            panelDefinition2.addPart(new PartDefinitionImpl(new DefaultPlaceRequest(key, placeParams)));
            p.getRoot().appendChild(new NamedPosition(key), panelDefinition2);
        });

        return p;
    }

    // ======

    // ====== Templated interface methods
    @Override
    public org.jboss.errai.common.client.dom.HTMLElement resolvePosition(NamedPosition p) {

        final String fieldName = p.getName();
        final HTMLElement element = this.componentContainersById.get(fieldName);
        if (element == null) {
            return null;
        }

        return Js.cast(element);
    }

    @Override
    public org.jboss.errai.common.client.dom.HTMLElement getRootElement() {
        return Js.cast(this.container);
    }
    // ======

    private Map<String, HTMLElement> loadTemplateComponents(final HTMLElement container) {

        final Map<String, HTMLElement> templateComponents = this.realPerspective.getContainerComponents(container)
                .stream()
                .map(element -> newPair(element.getAttribute(AF_JS_COMPONENT), element))
                .collect(toMap(Pair::getK1, Pair::getK2));

        templateComponents.values().forEach(component -> this.deepMarkComponentContainers(component, container));

        return templateComponents;
    }

    private void deepMarkComponentContainers(final Node leaf, final Node root) {

        // Run through every node between the root container and the component node marking it as an uf-perspective-component.
        // This is needed to make the TemplatedPresenter display the correct elements in the screen when it opens.

        if (!(leaf instanceof HTMLElement)) {
            return;
        }

        if (leaf == root) {
            return;
        }

        final HTMLElement htmlElement = (HTMLElement) leaf;
        if (!htmlElement.classList.contains(UF_PERSPECTIVE_COMPONENT)) {
            htmlElement.classList.add(UF_PERSPECTIVE_COMPONENT);
        }

        deepMarkComponentContainers(leaf.parentNode, root);
    }

    private Map<String, String> retrieveStartUpParams(final HTMLElement component) {

        final Map<String, String> params = new HashMap<>();

        for (int i = 0; i < component.attributes.length; i++) {

            final Attr attr = component.attributes.getAt(i);
            if (!attr.name.startsWith(STARTUP_PARAM_ATTR)) {
                continue;
            }

            final String key = attr.name.replaceFirst(STARTUP_PARAM_ATTR, "");
            if (key.length() > 0) {
                params.put(key, attr.value);
            }
        }

        return params;
    }
}