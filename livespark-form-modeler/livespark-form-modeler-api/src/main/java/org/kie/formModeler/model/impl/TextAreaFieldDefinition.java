package org.kie.formModeler.model.impl;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.formModeler.model.FieldDefinition;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
public class TextAreaFieldDefinition extends FieldDefinition<String> {

    protected Integer rows = 4;
    protected Integer cols = 15;
    protected String placeHolder;

    public Integer getRows() {
        return rows;
    }

    public void setRows( Integer rows ) {
        this.rows = rows;
    }

    public Integer getCols() {
        return cols;
    }

    public void setCols( Integer cols ) {
        this.cols = cols;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder( String placeHolder ) {
        this.placeHolder = placeHolder;
    }
}
