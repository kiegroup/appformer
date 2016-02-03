/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.widgets.crud.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;
import org.mvel2.util.Make;

public interface CrudConstants extends Messages {

    public static final CrudConstants INSTANCE = GWT.create(CrudConstants.class);

    public String newInstanceButton();

    public String newInstanceTitle();

    public String editInstanceButton();

    public String editInstanceTitle();

    public String deleteInstance();

    public String accept();

    public String cancel();

    public String deleteTitle();

    public String deleteBody();
}
