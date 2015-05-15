package org.livespark.formmodeler.rendering.client.shared.meta;

import org.livespark.formmodeler.rendering.client.shared.FormModel;

/**
 * Created by pefernan on 4/19/15.
 */
public abstract class DataHolderFieldMeta<F extends FormModel, T> {

    public abstract String getName();
    public abstract T getValue(F meta);
    public abstract void setValue(F meta, T value);
}
