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

package org.uberfire.ssh.client.editor.component;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.uberfire.ssh.client.editor.component.creation.NewSSHKeyModal;
import org.uberfire.ssh.client.editor.component.empty.SSHKeysEditorEmptyStateDisplayer;
import org.uberfire.ssh.client.editor.component.keys.SSHKeysDisplayer;
import org.uberfire.ssh.service.shared.editor.PortableSSHPublicKey;
import org.uberfire.ssh.service.shared.editor.SSHKeyEditorService;

@Dependent
public class SSHKeysEditor implements SSHKeysEditorView.Presenter,
                                      IsElement {

    private final SSHKeysEditorView view;
    private final SSHKeysDisplayer keysDisplayer;
    private final SSHKeysEditorEmptyStateDisplayer emptyStateDisplayer;
    private final NewSSHKeyModal newSSHKeyModal;
    private final Caller<SSHKeyEditorService> serviceCaller;

    @Inject
    public SSHKeysEditor(final SSHKeysEditorView view, final SSHKeysDisplayer keysDisplayer, final SSHKeysEditorEmptyStateDisplayer emptyStateDisplayer, final NewSSHKeyModal newSSHKeyModal, final Caller<SSHKeyEditorService> serviceCaller) {
        this.view = view;
        this.keysDisplayer = keysDisplayer;
        this.emptyStateDisplayer = emptyStateDisplayer;
        this.newSSHKeyModal = newSSHKeyModal;
        this.serviceCaller = serviceCaller;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        keysDisplayer.init(this::showNewKeyModal, this::delete);
        emptyStateDisplayer.init(this::showNewKeyModal);
        newSSHKeyModal.init(this::onAddKey);
    }

    public void load() {
        serviceCaller.call((RemoteCallback<Collection<PortableSSHPublicKey>>) this::loadKeys).getUserKeys();
    }

    private void loadKeys(Collection<PortableSSHPublicKey> keys) {
        clear();
        if (keys.isEmpty()) {
            view.show(emptyStateDisplayer.getElement());
        } else {
            keysDisplayer.render(keys);
            view.show(keysDisplayer.getElement());
        }
    }

    protected void delete(final PortableSSHPublicKey key) {
        serviceCaller.call((RemoteCallback<Void>) response -> load()).deleteKey(key);
    }

    protected void showNewKeyModal() {
        newSSHKeyModal.show();
    }

    protected void onAddKey() {
        newSSHKeyModal.hide();
        load();
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    @PreDestroy
    public void clear() {
        view.clear();
    }
}
