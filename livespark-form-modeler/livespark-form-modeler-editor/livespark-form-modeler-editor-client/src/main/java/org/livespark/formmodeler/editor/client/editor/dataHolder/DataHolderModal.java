package org.livespark.formmodeler.editor.client.editor.dataHolder;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Created by pefernan on 7/9/15.
 */
public class DataHolderModal extends Modal {
    private Button submit = new Button( "Submit" );
    private Button cancel = new Button( "Cancel" );

    DataHolderPanel formView;

    public DataHolderModal( final DataHolderPanel formView) {
        setHideOthers( false );
        setCloseVisible( true );

        this.formView = formView;
        add( formView );

        ModalFooter footer = new ModalFooter(  );
        footer.add( submit );
        footer.add( cancel );
        add( footer );
        cancel.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                hide();
            }
        } );
        this.show();
    }

    public void addSubmitClickHandler( ClickHandler handler ) {
        submit.addClickHandler( handler );
    }


}
