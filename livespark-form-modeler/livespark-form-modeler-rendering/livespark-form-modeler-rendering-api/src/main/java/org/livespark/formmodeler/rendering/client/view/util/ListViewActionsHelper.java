package org.livespark.formmodeler.rendering.client.view.util;

import org.livespark.formmodeler.rendering.client.shared.FormModel;

/**
 * Created by pefernan on 6/25/15.
 */
public interface ListViewActionsHelper<M extends FormModel> {
    public void create( M model );
    public void update( M model );
    public void delete( M model );
}
