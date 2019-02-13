package org.uberfire.jsbridge.client.perspective;

import org.uberfire.jsbridge.client.perspective.jsnative.JsNativeContextDisplay;
import org.uberfire.jsbridge.client.perspective.jsnative.JsNativePanel;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.impl.ContextDefinitionImpl;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;

import static java.util.stream.Collectors.toList;

public class JsWorkbenchPanelConverter {

    private final JsNativePanel nativePanel;

    public JsWorkbenchPanelConverter(final JsNativePanel nativePanel) {
        this.nativePanel = nativePanel;
    }

    public PanelDefinition toPanelDefinition() {

        final PanelDefinition newPanel = new PanelDefinitionImpl(nativePanel.panelType());
        newPanel.setPosition(nativePanel.position());

        final JsNativeContextDisplay contextDisplay = nativePanel.contextDisplay();

        newPanel.setContextDisplayMode(contextDisplay.mode());
        if (contextDisplay.contextId() != null) {
            newPanel.setContextDefinition(new ContextDefinitionImpl(new DefaultPlaceRequest(contextDisplay.contextId())));
        }

        if (nativePanel.width() > 0) {
            newPanel.setWidth(nativePanel.width());
        }

        if (nativePanel.minWidth() > 0) {
            newPanel.setMinWidth(nativePanel.minWidth());
        }

        if (nativePanel.height() > 0) {
            newPanel.setHeight(nativePanel.height());
        }

        if (nativePanel.minHeight() > 0) {
            newPanel.setHeight(nativePanel.minHeight());
        }

        nativePanel.view().parts().stream()
                .map(part -> new JsWorkbenchPartConverter(part).toPartDefinition())
                .forEach(newPanel::addPart);

        nativePanel.view().panels().stream()
                .map(panel -> new JsWorkbenchPanelConverter(panel).toPanelDefinition())
                .forEach(panel -> newPanel.insertChild(panel.getPosition(), panel));

        return newPanel;
    }
}
