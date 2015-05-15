package org.livespark.formmodeler.rendering.client.shared;

import java.util.ArrayList;
import java.util.List;

import org.livespark.formmodeler.rendering.client.shared.meta.DataHolderMeta;

public abstract class FormModel {
    protected List<String> fieldNames = new ArrayList<String>(  );
    protected List<DataHolderMeta> dataHolderMetas = new ArrayList<DataHolderMeta>(  );

    public DataHolderMeta[] getDataHolders() {
        return dataHolderMetas.toArray( new DataHolderMeta[ dataHolderMetas.size()] );
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }
}
