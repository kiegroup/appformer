/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.interpeter.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.kie.appformer.flow.api.Command;
import org.kie.appformer.flow.api.FormOperation;
import org.kie.appformer.flow.api.UIComponent;

public class TestUIComponent<M> implements UIComponent<M, Command<FormOperation, M>, TestFormView<M>> {

    private final TestFormView<M> component;
    private final Iterator<Function<M, Command<FormOperation, M>>> actions;

    public TestUIComponent( final Function<M, Command<FormOperation, M>>... actions ) {
        this.component = new TestFormView<>();
        this.actions = Arrays.asList( actions ).iterator();
    }

    @Override
    public void start( final M input,
                       final Consumer<Command<FormOperation, M>> callback ) {
        callback.accept( nextAction().apply( input ) );
    }

    private Function<M, Command<FormOperation, M>> nextAction() {
        if ( actions.hasNext() ) {
            return actions.next();
        }
        else {
            throw new AssertionError( "More actions called than specified." );
        }
    }

    @Override
    public void onHide() {
    }

    @Override
    public TestFormView<M> asComponent() {
        return component;
    }

    @Override
    public String getName() {
        return "TestUIComponent";
    }
}
