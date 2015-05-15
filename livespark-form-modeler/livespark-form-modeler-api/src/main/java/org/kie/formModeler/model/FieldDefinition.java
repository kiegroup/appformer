package org.kie.formModeler.model;

public abstract class FieldDefinition<T> {

    protected String name;

    protected String label;

    protected Boolean required = Boolean.FALSE;

    protected Boolean readonly = Boolean.FALSE;

    protected String bindingExpression;

    public String getCode() {
        return this.getClass().getName();
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel( String label ) {
        this.label = label;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired( Boolean required ) {
        this.required = required;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly( Boolean readonly ) {
        this.readonly = readonly;
    }

    public String getBindingExpression() {
        return bindingExpression;
    }

    public void setBindingExpression( String bindingExpression ) {
        this.bindingExpression = bindingExpression;
    }
}
