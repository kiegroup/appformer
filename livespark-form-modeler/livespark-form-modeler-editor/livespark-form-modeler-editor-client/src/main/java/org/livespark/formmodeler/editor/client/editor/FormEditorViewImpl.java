package org.livespark.formmodeler.editor.client.editor;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.widgets.metadata.client.KieEditorViewImpl;
import org.livespark.formmodeler.editor.client.editor.dataHolder.DataHolderModal;
import org.livespark.formmodeler.editor.client.editor.dataHolder.DataHolderPanel;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.uberfire.ext.layout.editor.client.LayoutEditorPlugin;

/**
 * Created by pefernan on 7/7/15.
 */
@Templated
public class FormEditorViewImpl extends KieEditorViewImpl implements FormEditorPresenter.FormEditorView {

    @Inject
    @DataField
    private Button createHolder;

    @Inject
    @DataField
    private FlowPanel content;

    @Inject
    DataHolderPanel dataHolderPanel;

    private FormEditorPresenter presenter;

    public FormEditorViewImpl() {
    }

    @PostConstruct
    protected void init() {
    }

    @Override
    public void loadContent( FormDefinition definition ) {

    }

    @Override
    public void setupLayoutEditor( LayoutEditorPlugin layoutEditorPluginAPI ) {
        content.clear();
        content.add( layoutEditorPluginAPI.asWidget() );
    }

    @EventHandler( "createHolder" )
    public void onCreateClick( ClickEvent event ) {
        presenter.getAvailableDataObjectsList();
    }

    @Override
    public void initDataHoldersPopup( List<String> availableDataHolders ) {
        dataHolderPanel.init( availableDataHolders );
        final DataHolderModal modal = new DataHolderModal( dataHolderPanel );
        modal.addSubmitClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                if (dataHolderPanel.validate()) {
                    modal.hide();
                    presenter.addDataHolder( dataHolderPanel.getDataHolderName(), dataHolderPanel.getDataHolderclass() );
                }
            }
        } );
    }

    @Override
    public void setPresenter( FormEditorPresenter presenter ) {
        this.presenter = presenter;
    }
}
