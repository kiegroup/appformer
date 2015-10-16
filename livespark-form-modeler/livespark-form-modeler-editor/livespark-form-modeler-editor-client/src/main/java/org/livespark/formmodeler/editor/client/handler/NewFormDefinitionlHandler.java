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
package org.livespark.formmodeler.editor.client.handler;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.widgets.client.handlers.DefaultNewResourceHandler;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.livespark.formmodeler.editor.client.resources.i18n.Constants;
import org.livespark.formmodeler.editor.client.type.FormDefinitionResourceType;
import org.livespark.formmodeler.editor.service.FormEditorService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.widgets.common.client.common.BusyPopup;
import org.uberfire.ext.widgets.common.client.common.popups.errors.ErrorPopup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.type.ResourceTypeDefinition;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class NewFormDefinitionlHandler extends DefaultNewResourceHandler {

    @Inject
    private Caller<FormEditorService> modelerService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private FormDefinitionResourceType resourceType;

    @Inject
    private Event<NotificationEvent> notificationEvent;

    @Override
    public String getDescription() {
        return Constants.INSTANCE.form_modeler_form();
    }

    @Override
    public IsWidget getIcon() {
        return null;
    }

    @Override
    public ResourceTypeDefinition getResourceType() {
        return resourceType;
    }

    @Override
    public void create( org.guvnor.common.services.project.model.Package pkg,
                        String baseFileName,
                        final NewResourcePresenter presenter ) {
        BusyPopup.showMessage( Constants.INSTANCE.creating_new_form() );

        modelerService.call( new RemoteCallback<Path>() {
                                 @Override
                                 public void callback( final Path path ) {
                                     BusyPopup.close();
                                     presenter.complete();
                                     notifySuccess();
                                     PlaceRequest place = new PathPlaceRequest( path, "LSFormEditor" );
                                     placeManager.goTo( place );

                                 }
                             }, new ErrorCallback<Message>() {
                                 @Override
                                 public boolean error( Message message,
                                                       Throwable throwable ) {
                                     BusyPopup.close();
                                     ErrorPopup.showMessage( CommonConstants.INSTANCE.SorryAnItemOfThatNameAlreadyExistsInTheRepositoryPleaseChooseAnother() );
                                     return true;
                                 }
                             }
                           ).createForm( pkg.getPackageMainResourcesPath(), buildFileName( baseFileName,
                                                                                           resourceType ) );
    }

}
