package org.livespark.formmodeler.rendering.client.view;

import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.google.gwt.user.client.ui.Composite;

public class ModalForm {

    private Modal m = new Modal();

    public ModalForm( final Composite composite, final String title, String id ) {
        m.setHideOthers( true );
        m.setCloseVisible( true );

        m.setTitle( title );
        m.add( composite );
        m.setBackdrop( BackdropType.NONE );
        m.getElement().setId( id );
    }

    public void show() {
        m.show();
        m.getElement().setAttribute( "style", "top: 40%; opacity: 0.9;" );
    }

    public void hide() {
        m.hide();
    }
}
