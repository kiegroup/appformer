package org.livespark.formmodeler.rendering.client.view;

import org.jboss.errai.common.client.api.annotations.LocalEvent;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

@LocalEvent
public class DeleteEvent<M extends FormModel> {

    private final M model;

    public DeleteEvent( M model ) {
        this.model = model;
    }

    public M getDeletedModel() {
        return model;
    }

}
