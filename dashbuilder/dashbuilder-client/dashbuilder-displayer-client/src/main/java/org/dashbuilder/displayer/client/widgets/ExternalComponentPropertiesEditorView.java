package org.dashbuilder.displayer.client.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Composite;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.dom.elemental2.Elemental2DomUtil;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.properties.editor.client.PropertyEditorWidget;
import org.uberfire.ext.properties.editor.model.PropertyEditorCategory;
import org.uberfire.ext.properties.editor.model.PropertyEditorChangeEvent;
import org.uberfire.ext.properties.editor.model.PropertyEditorEvent;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;

@Dependent
@Templated
public class ExternalComponentPropertiesEditorView extends Composite
                                                   implements ExternalComponentPropertiesEditor.View {

    private final String externalComponentEditorId = Document.get().createUniqueId();

    @Inject
    @DataField
    HTMLDivElement externalComponentPropertiesRoot;

    @Inject
    @DataField
    HTMLDivElement messageContainer;

    @Inject
    @DataField
    @Named("strong")
    HTMLElement messageTextContainer;

    @Inject
    Elemental2DomUtil elementalUtil;

    private PropertyEditorWidget propertyEditorWidget;

    private ExternalComponentPropertiesEditor presenter;

    @Override
    public void init(ExternalComponentPropertiesEditor presenter) {
        this.presenter = presenter;
        messageContainer.hidden = true;
        propertyEditorWidget = new PropertyEditorWidget();
        elementalUtil.appendWidgetToElement(externalComponentPropertiesRoot, propertyEditorWidget);
    }

    @Override
    public void componentNotFound() {
        showMessage("Component was not found.");
    }

    @Override
    public void addCategories(Collection<PropertyEditorCategory> categories) {
        messageContainer.hidden = true;
        externalComponentPropertiesRoot.hidden = false;
        List<PropertyEditorCategory> catList = new ArrayList<>(categories);
        propertyEditorWidget.handle(new PropertyEditorEvent(externalComponentEditorId, catList));
    }

    protected void onPropertyEditorChange(@Observes PropertyEditorChangeEvent event) {
        PropertyEditorFieldInfo property = event.getProperty();
        if (property.getEventId().equalsIgnoreCase(externalComponentEditorId)) {
            presenter.onPropertyChange(property.getKey(), property.getCurrentStringValue());
        }
    }

    @Override
    public void noPropertiesComponent() {
        showMessage("This component has no properties.");
    }

    private void showMessage(String message) {
        messageContainer.hidden = false;
        externalComponentPropertiesRoot.hidden = true;
        messageTextContainer.textContent = message;
    }

}