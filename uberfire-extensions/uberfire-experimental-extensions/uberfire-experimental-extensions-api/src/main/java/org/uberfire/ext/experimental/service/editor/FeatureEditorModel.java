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

package org.uberfire.ext.experimental.service.editor;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class FeatureEditorModel {

    private List<EditableFeature> features;

    public FeatureEditorModel(@MapsTo("features") List<EditableFeature> features) {
        this.features = features;
    }

    public List<EditableFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<EditableFeature> features) {
        this.features = features;
    }
}
