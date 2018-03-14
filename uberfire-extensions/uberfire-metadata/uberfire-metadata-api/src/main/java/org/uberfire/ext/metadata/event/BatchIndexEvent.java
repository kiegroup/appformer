/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.uberfire.ext.metadata.event;

import java.util.List;

import org.uberfire.ext.metadata.model.KObject;

/**
 * <p>
 * An event fired when a batch of {@link KObject KObjects} has been indexed.
 *
 * <p>
 * Items should be available for lookup when this item is observed.
 */
public class BatchIndexEvent {

    private final List<IndexEvent> events;

    public BatchIndexEvent(List<IndexEvent> events) {
        this.events = events;
    }

    public List<IndexEvent> getIndexEvents() {
        return events;
    }

}
