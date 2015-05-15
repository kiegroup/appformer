package org.livespark.formmodeler.codegen.services.datamodeller;

import org.kie.workbench.common.screens.datamodeller.model.DataObjectTO;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.uberfire.backend.vfs.Path;

/**
 * Created by pefernan on 4/29/15.
 */
public interface DataModellerFormGenerator {
    public void generateFormForDataObject( DataObject dataObject, Path path );
}
