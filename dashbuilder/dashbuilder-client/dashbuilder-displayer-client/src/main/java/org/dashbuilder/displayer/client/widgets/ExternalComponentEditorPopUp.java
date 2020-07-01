package org.dashbuilder.displayer.client.widgets;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;
import org.uberfire.mvp.Command;

@Dependent
public class ExternalComponentEditorPopUp extends BaseModal {

    @Inject
    ExternalComponentEditor externalComponentEditor;
    private Command closeCommand;
    private Command saveCommand;

    @PostConstruct
    public void setup() {
        ModalFooterOKCancelButtons footer = createModalFooterOKCancelButtons();
        footer.enableCancelButton(true);
        footer.enableOkButton(true);
        setBody(externalComponentEditor.asWidget());
        add(footer);
        setTitle("Component Editor");
        setWidth(1200 + "px");
    }

    public void init(String componentId, Map<String, String> properties, Command closeCommand, Command saveCommand) {
        this.closeCommand = closeCommand;
        this.saveCommand = saveCommand;
        this.addShowHandler(e -> externalComponentEditor.withComponent(componentId, properties));
        show();
    }

    protected ModalFooterOKCancelButtons createModalFooterOKCancelButtons() {
        return new ModalFooterOKCancelButtons(() -> {
            hide();
            saveCommand.execute();

        }, () -> {
            hide();
            closeCommand.execute();
        });
    }

    public Map<String, String> getProperties() {
        return externalComponentEditor.getNewProperties();
    }

}