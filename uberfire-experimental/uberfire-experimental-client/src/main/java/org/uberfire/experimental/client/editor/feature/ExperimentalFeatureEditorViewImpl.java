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

package org.uberfire.experimental.client.editor.feature;

import javax.inject.Inject;

import com.google.gwt.event.dom.client.ChangeEvent;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;
import org.jboss.errai.common.client.dom.elemental2.Elemental2DomUtil;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.client.views.pfly.widgets.JQueryProducer;
import org.uberfire.client.views.pfly.widgets.Popover;

@Templated
public class ExperimentalFeatureEditorViewImpl implements ExperimentalFeatureEditorView,
                                                          IsElement {

    private Presenter presenter;

    @Inject
    @DataField
    private HTMLDivElement container;

    @Inject
    @DataField
    private HTMLLabelElement name;

    @Inject
    @DataField
    private HTMLAnchorElement helpMessage;

    @Inject
    @DataField
    private ToggleSwitch enabled;

    @Inject
    private JQueryProducer.JQuery<Popover> jQueryPopover;

    @Inject
    private Elemental2DomUtil util;

    @Override
    public void init(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void render(String name, String description, boolean enabled) {
        this.name.textContent = name;
        this.enabled.setValue(enabled);

        if (description != null) {
            helpMessage.setAttribute("data-content", description);
            jQueryPopover.wrap(util.asHTMLElement(helpMessage)).popover();
        } else {
            container.removeChild(helpMessage);
        }
    }

    @EventHandler("enabled")
    public void onToggleChange(ChangeEvent event) {
        presenter.notifyChange(enabled.getValue());
    }
}
