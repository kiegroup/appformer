/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
