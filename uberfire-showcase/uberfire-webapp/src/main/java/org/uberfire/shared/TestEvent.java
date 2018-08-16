/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.shared;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TestEvent {

    private String foo;

//    private Map<String, TestEvent> testMap = new HashMap<String, TestEvent>() {{
//        put("foo", new TestEvent("foo1", null));
//        put("bar", new TestEvent("bar1", new TestEvent("bar2", null)));
//    }};

    private TestEvent child;

    public TestEvent() {
    }

    public TestEvent(final String foo, final TestEvent child) {
        this.foo = foo;
        this.child = child;
    }

    public String getFoo() {
        return foo;
    }

    public TestEvent getChild() {
        return child;
    }

//    public Map<String, TestEvent> getTestMap() {
//        return testMap;
//    }
}
