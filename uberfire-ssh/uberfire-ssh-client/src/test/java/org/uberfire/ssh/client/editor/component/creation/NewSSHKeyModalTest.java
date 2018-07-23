/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ssh.client.editor.component.creation;

import org.assertj.core.api.Assertions;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.Command;
import org.uberfire.ssh.client.resources.i18n.AppformerSSHConstants;
import org.uberfire.ssh.service.backend.keystore.util.PublicKeyConverter;
import org.uberfire.ssh.service.shared.editor.SSHKeyEditorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class NewSSHKeyModalTest {

    private static final String NAME = "name";
    private static final String WRONG_KEY = "wrong key";
    private static final String WRONG_KEY_FORMAT = "ssh-rsa wrong key";
    private static final String VALID_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDNsKIMkhaI8iX69IKsux/LdgG3zP1wW5RNJz" +
            "bopy7BLqZEmqTZMIfaWEisuH5WZquG3tZ//yNrVNd0Jw5RYQ/fwkyVmmJi9Ir2bo5rex4jbkNwcWb8U57UpIt38JCjjKLCkiYSKNXzrJOm" +
            "tFsMOuHukoGJbSRLDV3VSmQVIbtrysz8CRCCg2bv2KZkTmKa50O4S0UpjEVeyuy/+sDqbKl9Jrhj0i7PFrB1hJhN4+7SnNDAr6OpdZd0EU" +
            "Ua1TNdDISsdetq9vWMnuYBQPlxHxXnJsJhvdIlLXW6ZfZpsjqxe8jfHsJtmFvD032w/B4kBfGZxQXbPoUUBdrGyrKb2FyAypdDxAotA1Rl" +
            "sq3S6PWBlp7RjpMYWZb02XqNrN6g6AJCh0uuWCK/jxO6S96MYFyJj7rqUgaRg7SEKwR2lhwWTzUxb5bxbNxsA4eUXnvSr0lqCwcjw3M5WQ" +
            "HocGn4VPjKZl7Jhqxu9evwF5siuZEDL4oK8NgPwAZxMYcFuefdPgpxA/wmqWAh6JPbXLstQlG24bTrxCIzsx7qEfhU65KQJaLi3kso4LA/" +
            "IDmPRHIFGNUbY3YOwfDpmH/fHFQNY/5uy5/0oICAv9M3QBEMvB2rWpWJT8j2CkISCSjzPNnB490uUv9cxNnLs8tDrOHlAnm+k0iXyJ4hjq" +
            "tXqSbLCLz2Jw== katy";

    @Mock
    private NewSSHKeyModalView view;

    @Mock
    private SSHKeyEditorService sshKeyEditorService;

    private CallerMock<SSHKeyEditorService> serviceCaller;

    @Mock
    private TranslationService translationService;

    @Mock
    private Command addCommand;

    private NewSSHKeyModal modal;

    @Before
    public void init() {
        when(translationService.format(eq(AppformerSSHConstants.ValidationCannotBeEmpty), any())).thenReturn("Field cannot be empty");

        doAnswer((Answer<Void>) invocationOnMock -> {
            String keyContent = (String) invocationOnMock.getArguments()[1];

            PublicKeyConverter.fromString(keyContent);
            return null;
        }).when(sshKeyEditorService).addKey(anyString(), anyString());

        serviceCaller = new CallerMock<>(sshKeyEditorService);

        modal = new NewSSHKeyModal(view, serviceCaller, translationService);

        verify(view).init(modal);
    }

    @Test
    public void testBasicFunctions() {

        modal.init(addCommand);

        modal.show();

        verify(view).show();

        modal.hide();

        verify(view).hide();
    }

    @Test
    public void testNotifyCancel() {

        modal.notifyCancel();

        verify(view).hide();
    }

    @Test
    public void testAddKeyNullValidationFailure() {
        testBasicFunctions();

        modal.notifyAdd(null, null);

        verify(view).resetValidation();

        verify(translationService).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplName));
        verify(translationService).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplKey));
        verify(translationService, times(2)).format(eq(AppformerSSHConstants.ValidationCannotBeEmpty), any());
        verify(translationService, never()).getTranslation(AppformerSSHConstants.ValidationKeyFormatError);

        verify(view).setNameValidationError(any());
        verify(view).setKeyValidationError(any());

        verify(sshKeyEditorService, never()).addKey(anyString(), anyString());
        verify(addCommand, never()).execute();
    }

    @Test
    public void testAddKeyWrongKeyValidationFailure() {
        testBasicFunctions();

        modal.notifyAdd(NAME, WRONG_KEY);

        verify(view).resetValidation();

        verify(translationService, never()).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplName));
        verify(translationService, never()).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplKey));
        verify(translationService, never()).format(eq(AppformerSSHConstants.ValidationCannotBeEmpty), any());
        verify(translationService).getTranslation(AppformerSSHConstants.ValidationKeyFormatError);

        verify(view, never()).setNameValidationError(any());
        verify(view).setKeyValidationError(any());

        verify(sshKeyEditorService, never()).addKey(anyString(), anyString());
        verify(addCommand, never()).execute();
    }

    @Test
    public void testAddKeyWrongKeyFormatValidationFailure() {
        testBasicFunctions();

        modal.notifyAdd(NAME, WRONG_KEY_FORMAT);

        verify(view).resetValidation();

        verify(translationService, never()).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplName));
        verify(translationService, never()).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplKey));
        verify(translationService, never()).format(eq(AppformerSSHConstants.ValidationCannotBeEmpty), any());

        verify(view, never()).setNameValidationError(any());

        verify(sshKeyEditorService).addKey(anyString(), anyString());
        verify(translationService).getTranslation(AppformerSSHConstants.ValidationKeyFormatError);
        verify(view).setKeyValidationError(any());
        verify(addCommand, never()).execute();
    }

    @Test
    public void testAddKey() {
        testBasicFunctions();

        modal.notifyAdd(NAME, VALID_KEY);

        verify(view).resetValidation();

        verify(translationService, never()).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplName));
        verify(translationService, never()).getTranslation(eq(AppformerSSHConstants.NewSSHKeyModalViewImplKey));
        verify(translationService, never()).format(eq(AppformerSSHConstants.ValidationCannotBeEmpty), any());
        verify(translationService, never()).getTranslation(AppformerSSHConstants.ValidationKeyFormatError);

        verify(view, never()).setNameValidationError(any());
        verify(view, never()).setKeyValidationError(any());

        verify(sshKeyEditorService).addKey(anyString(), anyString());
        verify(addCommand).execute();
    }

    @Test
    public void testInitNull() {
        Assertions.assertThatThrownBy(() -> modal.init(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter named 'addCommand' should be not null!");
    }
}
