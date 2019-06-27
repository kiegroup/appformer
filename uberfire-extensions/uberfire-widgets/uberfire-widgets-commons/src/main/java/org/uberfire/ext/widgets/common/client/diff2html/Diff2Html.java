/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.ext.widgets.common.client.diff2html;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Composite;

public class Diff2Html extends Composite {

    private String containerId;

    private DiffOutputFormat outputFormat;

    private String diffText;

    private boolean highlightCode;

    private JavaScriptObject viewer;

    public Diff2Html(final String containerId,
                     final DiffOutputFormat outputFormat,
                     final String diffText,
                     final boolean highlightCode) {
        this.diffText = diffText;
        this.highlightCode = highlightCode;
        this.outputFormat = outputFormat;
        this.setupContainerId(containerId);
        this.initialize();
    }

    private void setupContainerId(String containerId) {
        this.containerId = "#" + containerId;
    }

    private native void initialize()/*-{
        var diffText = this.@org.uberfire.ext.widgets.common.client.diff2html.Diff2Html::diffText;

        this.@org.uberfire.ext.widgets.common.client.diff2html.Diff2Html::viewer = new $wnd.Diff2HtmlUI({diff: diffText});
    }-*/;

    public native void draw() /*-{
        var inputFormat = "diff";
        var matching = "none";
        var showFiles = false;

        var containerId = this.@org.uberfire.ext.widgets.common.client.diff2html.Diff2Html::containerId;
        var outputFormat = this.@org.uberfire.ext.widgets.common.client.diff2html.Diff2Html::outputFormat.toString();

        var highlightCode = this.@org.uberfire.ext.widgets.common.client.diff2html.Diff2Html::highlightCode;

        var viewer = this.@org.uberfire.ext.widgets.common.client.diff2html.Diff2Html::viewer;

        viewer.draw(containerId, {
            inputFormat: inputFormat,
            showFiles: showFiles,
            matching: matching,
            outputFormat: outputFormat
        });

        if (highlightCode) {
            viewer.highlightCode(containerId);
        }
    }-*/;
}
