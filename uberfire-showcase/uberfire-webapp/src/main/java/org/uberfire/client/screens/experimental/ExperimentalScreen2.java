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

package org.uberfire.client.screens.experimental;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.experimental.definition.annotations.ExperimentalFeature;

@WorkbenchScreen(identifier = ExperimentalScreen2.ID)
@ExperimentalFeature(nameI18nKey = "experimental_screen2", descriptionI18nKey = "experimental_screen2_description")
public class ExperimentalScreen2 implements IsElement {

    public static final String ID = "ExperimentalPerspectiveScreen2";

    private static final String TITLE = "Second Experimental Screen";

    @Inject
    @Named("h1")
    private HTMLHeadingElement header;

    @PostConstruct
    public void init() {
        header.textContent = TITLE;
        header.style.textAlign = "center";
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return TITLE;
    }

    @WorkbenchPartView
    public IsElement getView() {
        return this;
    }

    @Override
    public HTMLElement getElement() {
        return header;
    }
}
