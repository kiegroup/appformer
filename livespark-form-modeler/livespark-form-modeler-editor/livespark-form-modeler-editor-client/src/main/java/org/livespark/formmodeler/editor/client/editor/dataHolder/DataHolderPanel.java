package org.livespark.formmodeler.editor.client.editor.dataHolder;

import java.util.List;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by pefernan on 7/9/15.
 */
@Templated
public class DataHolderPanel extends Composite {

    //@DataField
    //ControlGroup dataHolderNameGroup;

    @Inject
    @DataField
    private TextBox dataHolderName;

    //@DataField
    //ControlGroup dataHolderClassGroup;

    @Inject
    @DataField
    private ListBox dataHolderClass;

    public void init( List<String> classes ) {
      //  dataHolderNameGroup.setType( ControlGroupType.NONE );
      //  dataHolderClassGroup.setType( ControlGroupType.NONE );
        dataHolderName.setValue( "" );
        dataHolderClass.clear();
        if (classes == null) return;

        for (String className : classes) {
            dataHolderClass.addItem( className );
        }
    }

    public String getDataHolderName() {
        return dataHolderName.getText();
    }

    public String getDataHolderclass() {
        return dataHolderClass.getValue();
    }

    public boolean validate() {
        boolean valid = true;
        String value = getDataHolderName();
        if (value == null || value.isEmpty()) {
        //    dataHolderNameGroup.setType( ControlGroupType.ERROR );
            valid = false;
        } else {
        //    dataHolderNameGroup.setType( ControlGroupType.NONE );
        }

        value = getDataHolderclass();
        if (value == null || value.isEmpty()) {
        //    dataHolderClassGroup.setType( ControlGroupType.ERROR );
            valid = false;
        } else {
        //    dataHolderClassGroup.setType( ControlGroupType.NONE );
        }

        return valid;
    }
}
