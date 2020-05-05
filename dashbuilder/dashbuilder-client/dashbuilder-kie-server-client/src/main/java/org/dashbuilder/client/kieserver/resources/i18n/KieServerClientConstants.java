/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.dashbuilder.client.kieserver.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface KieServerClientConstants extends Messages {

    public static final KieServerClientConstants INSTANCE = GWT.create(KieServerClientConstants.class);

    public String remote_data_set_editor();

    public String remote_data_set_editor_description();

    public String remote_query_target_hint();

    public String remote_query_target();

    public String remote_server_template_hint();

    public String remote_server_template();

    public String remote_server_template_description();

    public String remote_datasource_description();

    public String remote_query_target_description();

    public String remote_source_description();
    
    public String remote_query_placeHolder();
    
    public String remote_sql_source();
    
    public String remote_sql_datasource();

}