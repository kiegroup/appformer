package org.livespark.formmodeler.rendering.client.view;

import org.jboss.errai.common.client.api.annotations.LocalEvent;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

@LocalEvent
public class EditEvent<M extends FormModel> {

    private final M model;

    public EditEvent( M model ) {
        this.model = model;
    }

    public M getModel() {
        return model;
    }
}
