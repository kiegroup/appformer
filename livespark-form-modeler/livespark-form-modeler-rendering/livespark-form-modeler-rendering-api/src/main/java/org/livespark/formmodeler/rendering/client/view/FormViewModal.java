package org.livespark.formmodeler.rendering.client.view;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Created by pefernan on 6/25/15.
 */
public class FormViewModal extends Modal {

    private Button submit = new Button( "Submit" );
    private Button cancel = new Button( "Cancel" );

    FormView formView;

    public FormViewModal( final FormView formView, final String title, String id ) {
        setHideOthers( false );
        setCloseVisible( true );
        setTitle( title );
        setBackdrop( BackdropType.NONE );
        getElement().setId( id );

        this.formView = formView;
        add( formView );

        ModalFooter footer = new ModalFooter(  );
        footer.add( submit );
        footer.add( cancel );
        add( footer );
    }

    public void addSubmitClickHandler( ClickHandler handler ) {
        submit.addClickHandler( handler );
    }

    public void addCancelClickHandler( ClickHandler handler ) {
        cancel.addClickHandler( handler );
    }
}
