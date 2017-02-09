package org.kie.appformer.client.editor;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

@Templated
public class TextEditorView extends Composite implements TextEditorPresenter.View {

    @Inject
    @DataField("text-area")
    private TextArea textArea;

    TextEditorPresenter presenter;
    protected Label title = new Label();
    private String originalContent = null;
    private boolean dirty = false;

    @Override
    public void init(final TextEditorPresenter presenter) {
        this.presenter = presenter;
        textArea.getElement().getStyle().setPosition( Position.ABSOLUTE );
        textArea.getElement().getStyle().setTop(0.0, Unit.PX);
        textArea.getElement().getStyle().setBottom(0.0, Unit.PX);
        textArea.getElement().getStyle().setWidth( 100, Unit.PCT );
        textArea.getElement().getStyle().setHeight( 100, Unit.PCT );
    }

    @Override
    public IsWidget getTitleWidget() {
        return title;
    }

    @Override
    public void setContent(final String content) {
        originalContent = content;
        textArea.setText(content);
        dirty = false;
    }

    @Override
    public String getContent() {
        return textArea.getText();
    }

    @Override
    public boolean isDirty() {
        if (originalContent!=null && !originalContent.equals(getContent()))
            return true;
        return dirty;
    }

    @Override
    public void setDirty(final boolean dirty) {
        this.dirty = dirty;
        if (!dirty) {
            originalContent = getContent();
        }
    }
}