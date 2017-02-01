/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.provisioning.client.wizard;

import java.util.Arrays;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Label;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.appformer.provisioning.client.resources.i18n.AppFormerProvisioningConstants;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;

import static org.jboss.errai.common.client.dom.DOMUtil.*;

@Dependent
@Templated
public class ConfigureServerPageViewImpl
        implements ConfigureServerPageView, IsElement {

    private static final String HAS_ERROR = "has-error";

    private static final String HAS_SUCCESS = "has-success";

    private static final String INFO = "alert-info";

    private static final String INFO_ICON = "pficon-info";

    private static final String SUCCESS = "alert-success";

    private static final String SUCCESS_ICON = "pficon-ok";

    private static final String DANGER = "alert-danger";

    private static final String DANGER_ICON = "pficon-error-circle-o";

    @Inject
    @DataField( "form-status-inline" )
    private Div formStatusInline;

    @Inject
    @DataField( "form-status-inline-icon" )
    private Span formStatusInlineIcon;

    @Inject
    @DataField( "form-status-inline-message" )
    private Span formStatusInlineMessage;

    @Inject
    @DataField( "host-form-group" )
    private Div hostFormGroup;

    @Inject
    @DataField( "host-label" )
    private Label hostLabel;

    @Inject
    @DataField( "host-text-input" )
    private TextInput hostTextInput;

    @Inject
    @DataField( "host-text-help" )
    private Span hostTextHelp;

    @Inject
    @DataField( "port-form-group" )
    private Div portFormGroup;

    @Inject
    @DataField( "port-label" )
    private Label portLabel;

    @Inject
    @DataField( "port-text-input" )
    private TextInput portTextInput;

    @Inject
    @DataField( "port-text-help" )
    private Span portTextHelp;

    @Inject
    @DataField( "management-port-form-group" )
    private Div managementPortFormGroup;

    @Inject
    @DataField( "management-port-label" )
    private Label managementPortLabel;

    @Inject
    @DataField( "management-port-text-input" )
    private TextInput managementPortTextInput;

    @Inject
    @DataField( "management-port-text-help" )
    private Span managementPortTextHelp;

    @Inject
    @DataField( "management-realm-form-group" )
    private Div managementRealmFormGroup;

    @Inject
    @DataField( "management-reaml-label" )
    private Label managementRealmLabel;

    @Inject
    @DataField( "management-realm-text-input" )
    private TextInput managementrRealmTextInput;

    @Inject
    @DataField( "management-realm-text-help" )
    private Span managementRealTextHelp;

    @Inject
    @DataField( "management-user-form-group" )
    private Div managementUserFormGroup;

    @Inject
    @DataField( "management-user-label" )
    private Label managementUserLabel;

    @Inject
    @DataField( "management-user-text-input" )
    private TextInput managementUserTextInput;

    @Inject
    @DataField( "management-user-text-help" )
    private Span managementUserTextHelp;

    @Inject
    @DataField( "management-password-form-group" )
    private Div managementPasswordFormGroup;

    @Inject
    @DataField( "management-password-label" )
    private Label managementPasswordLabel;

    @Inject
    @DataField( "management-password-text-input" )
    private TextInput managementPasswordTextInput;

    @Inject
    @DataField( "management-password-text-help" )
    private Span managementPasswordTextHelp;

    @Inject
    @DataField( "test-connection-button" )
    private Button testConnectionButton;

    @Inject
    private TranslationService translationService;

    private Presenter presenter;

    public ConfigureServerPageViewImpl( ) {
    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public String getPageTitle( ) {
        return translationService.getTranslation( AppFormerProvisioningConstants.ConfigureServerPage_title );
    }

    @Override
    public String getHost( ) {
        return hostTextInput.getValue( );
    }

    @Override
    public void setHost( String host ) {
        hostTextInput.setValue( host );
    }

    @Override
    public void setHostErrorMessage( String errorMessage ) {
        setErrorMessage( hostFormGroup, hostTextHelp, errorMessage );
    }

    @Override
    public void clearHostErrorMessage( ) {
        clearErrorMessage( hostFormGroup, hostTextHelp );
    }

    @Override
    public String getPort( ) {
        return portTextInput.getValue( );
    }

    @Override
    public void setPort( String port ) {
        this.portTextInput.setValue( port );
    }

    @Override
    public void setPortErrorMessage( String errorMessage ) {
        setErrorMessage( portFormGroup, portTextHelp, errorMessage );
    }

    @Override
    public void clearPortErrorMessage( ) {
        clearErrorMessage( portFormGroup, portTextHelp );
    }

    @Override
    public String getManagementPort( ) {
        return managementPortTextInput.getValue( );
    }

    @Override
    public void setManagementPort( String managementPort ) {
        this.managementPortTextInput.setValue( managementPort );
    }

    @Override
    public void setManagementPortErrorMessage( String errorMessage ) {
        setErrorMessage( managementPortFormGroup, managementPortTextHelp, errorMessage );
    }

    @Override
    public void clearManagementPortErrorMessage( ) {
        clearErrorMessage( managementPortFormGroup, managementPortTextHelp );
    }

    @Override
    public String getManagementRealm( ) {
        return managementrRealmTextInput.getValue( );
    }

    @Override
    public void setManagementRealm( String managementRealm ) {
        this.managementrRealmTextInput.setValue( managementRealm );
    }

    @Override
    public void setManagementRealmErrorMessage( String errorMessage ) {
        setErrorMessage( managementRealmFormGroup, managementRealTextHelp, errorMessage );
    }

    @Override
    public void clearManagementRealmErrorMessage( ) {
        clearErrorMessage( managementRealmFormGroup, managementRealTextHelp );
    }

    @Override
    public String getManagementUser( ) {
        return managementUserTextInput.getValue( );
    }

    @Override
    public void setManagementUser( String managementUser ) {
        this.managementUserTextInput.setValue( managementUser );
    }

    @Override
    public void setManagementUserErrorMessage( String errorMessage ) {
        setErrorMessage( managementUserFormGroup, managementUserTextHelp, errorMessage );
    }

    @Override
    public void clearManagementUserErrorMessage( ) {
        clearErrorMessage( managementUserFormGroup, managementUserTextHelp );
    }

    @Override
    public String getManagementPassword( ) {
        return managementPasswordTextInput.getValue( );
    }

    @Override
    public void setManagementPassword( String managementPassword ) {
        this.managementPasswordTextInput.setValue( managementPassword );
    }

    @Override
    public void setManagementPasswordErrorMessage( String errorMessage ) {
        setErrorMessage( managementPasswordFormGroup, managementPasswordTextHelp, errorMessage );
    }

    @Override
    public void clearManagementPasswordErrorMessage( ) {
        clearErrorMessage( managementPasswordFormGroup, managementPasswordTextHelp );
    }

    @Override
    public void showMessage( String title, String message ) {
        YesNoCancelPopup.newYesNoCancelPopup( title, message, null, null, ( org.uberfire.mvp.Command ) ( ) -> {
            //do nothing.
        } ).show( );
    }

    @Override
    public void setFormStatusInfoMessage( String message ) {
        clearFormStatusClasses( );
        addCSSClass( formStatusInline, INFO );
        addCSSClass( formStatusInlineIcon, INFO_ICON );
        formStatusInlineMessage.setTextContent( message );
    }

    @Override
    public void setFormStatusErrorMessage( String message ) {
        clearFormStatusClasses( );
        addCSSClass( formStatusInline, DANGER );
        addCSSClass( formStatusInlineIcon, DANGER_ICON );
        formStatusInlineMessage.setTextContent( message );
    }

    @Override
    public void setFormStatusSuccessMessage( String message ) {
        clearFormStatusClasses( );
        addCSSClass( formStatusInline, SUCCESS );
        addCSSClass( formStatusInlineIcon, SUCCESS_ICON );
        formStatusInlineMessage.setTextContent( message );
    }

    private void setErrorMessage( final Div formGroup, final Span span, final String text ) {
        removeCSSClass( formGroup, HAS_SUCCESS );
        addCSSClass( formGroup, HAS_ERROR );
        span.setTextContent( text );
    }

    private static void clearErrorMessage( final Div formGroup, final Span span ) {
        removeCSSClass( formGroup, HAS_ERROR );
        removeCSSClass( formGroup, HAS_SUCCESS );
        span.setTextContent( "" );
    }

    private void removeClasses( HTMLElement element, String... classes ) {
        Arrays.stream( classes ).forEach( clazz -> removeCSSClass( element, clazz ) );
    }

    private void clearFormStatusClasses( ) {
        removeClasses( formStatusInline, INFO, SUCCESS, DANGER );
        removeClasses( formStatusInlineIcon, INFO_ICON, SUCCESS_ICON, DANGER_ICON );
    }

    @EventHandler( "port-text-input" )
    private void onPortChange( @ForEvent( "change" ) Event event ) {
        presenter.onPortChange( );
    }

    @EventHandler( "host-text-input" )
    private void onHostChange( @ForEvent( "change" ) Event event ) {
        presenter.onHostChange( );
    }

    @EventHandler( "management-port-text-input" )
    private void onManagementPortChange( @ForEvent( "change" ) Event event ) {
        presenter.onManagementPortChange( );
    }

    @EventHandler( "management-realm-text-input" )
    private void onManagementRealmChange( @ForEvent( "change" ) Event event ) {
        presenter.onManagementRealmChange( );
    }

    @EventHandler( "management-user-text-input" )
    private void onManagementUserChange( @ForEvent( "change" ) Event event ) {
        presenter.onManagementUserChange( );
    }

    @EventHandler( "management-password-text-input" )
    private void onManagementPasswordChange( @ForEvent( "change" ) Event event ) {
        presenter.onManagementPasswordChange( );
    }

    @EventHandler( "test-connection-button" )
    private void onTestConnection( @ForEvent( "click" ) Event event ) {
        presenter.onTestConnection( );
    }
}