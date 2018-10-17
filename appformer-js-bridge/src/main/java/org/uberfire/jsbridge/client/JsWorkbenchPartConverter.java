package org.uberfire.jsbridge.client;

import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PartDefinition;
import org.uberfire.workbench.model.impl.ContextDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;

public class JsWorkbenchPartConverter {

    private final JsNativePart nativePart;

    public JsWorkbenchPartConverter(final JsNativePart nativePart) {
        this.nativePart = nativePart;
    }

    public PartDefinition toPartDefinition() {

        final PlaceRequest placeRequest = new DefaultPlaceRequest(nativePart.placeName(), nativePart.parameters());

        final PartDefinition partDefinition = new PartDefinitionImpl(placeRequest);
        partDefinition.setContextDisplayMode(nativePart.contextDisplayMode());
        if (nativePart.contextId() != null) {
            partDefinition.setContextDefinition(new ContextDefinitionImpl(new DefaultPlaceRequest(nativePart.contextId())));
        }

        return partDefinition;
    }
}
