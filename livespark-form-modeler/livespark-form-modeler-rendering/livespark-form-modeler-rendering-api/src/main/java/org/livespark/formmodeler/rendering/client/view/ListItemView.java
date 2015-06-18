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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;

public abstract class ListItemView<M extends FormModel> extends BaseView<M> {

    @Inject
    @DataField
    protected Button delete;

    @Inject
    @DataField
    protected Button edit;

    @Inject
    protected Event<DeleteEvent<M>> deleteEvent;

    @Inject
    protected Event<EditEvent<M>> editEvent;

    @EventHandler("edit")
    protected void onEdit( ClickEvent e ) {
        editEvent.fire( new EditEvent<M>( getModel() ) );
    }

    @EventHandler("delete")
    protected void onDelete( ClickEvent e ) {
        deleteEvent.fire( new DeleteEvent<M>( getModel() ) );
    }
}
