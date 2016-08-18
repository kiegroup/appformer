/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.livespark.flow.client.local;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.errai.ioc.client.container.Factory;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.cdi.api.FlowComponent;
import org.livespark.flow.cdi.api.FlowInput;
import org.livespark.flow.cdi.api.FlowOutput;

/**
 * <p>
 * Takes types annotated with {@link FlowComponent} and turns them into {@link UIComponent UIComponents}.
 */
@ApplicationScoped
public class CDIFlowComponentFactory {

    public static abstract class BaseFlowIO {
        private Object key;
        public void setKey( final Object key ) {
            this.key = key;
        }
        public boolean hasKey() {
            return key != null;
        }
        public Object getKey() {
            if ( !hasKey() ) {
                throw new IllegalStateException( "Cannot access key for FlowInput/FlowOutput before it is set." );
            }
            return key;
        }
    }

    private final class FlowOutputImpl<T> extends BaseFlowIO implements FlowOutput<T> {
        @Override
        public void submit( final T output ) {
            consumeOutput( getKey(), output );
        }
    }

    private final class FlowInputImpl<T> extends BaseFlowIO implements FlowInput<T> {
        @Override
        @SuppressWarnings( "unchecked" )
        public T get() {
            return (T) getInput( getKey() );
        }
    }

    private static class Frame {
        final Object instance;
        final Object input;
        final Consumer<?> callback;
        final Consumer<?> closer;
        Frame( final Object instance, final Object input, final Consumer<?> callback, final Consumer<?> closer ) {
            this.instance = instance;
            this.input = input;
            this.callback = callback;
            this.closer = closer;
        }
    }

    private final Map<Object, Frame> frames = new IdentityHashMap<>();

    /**
     * Create {@link UIComponent} from a type annotated with {@link FlowComponent}.
     *
     * @param instanceSupplier
     *            Provides instances of they type annotated with {@link FlowComponent}.
     * @param starter
     *            Starts a UI component. Code in the starter may call the {@link FlowInput} of the
     *            type annotated with {@link FlowComponent}.
     * @param destroy
     *            Cleans up this component and any associated resources. Once this is called, a
     *            component is no longer in service.
     * @param name
     *            A name for this component used in logging and error messages.
     * @param <T>
     *            A type annotated with {@link FlowComponent} that injects a {@link FlowInput} and
     *            {@link FlowOutput}.
     * @param <INPUT>
     *            The input type of the component. Should match the type argument of the
     *            {@link FlowInput} injected into the type {@code T}.
     * @param <OUTPUT>
     *            The output type of the component. Should match the type argument of the
     *            {@link FlowOutput} injected into the type {@code T}.
     * @param <COMPONENT>
     *            The type of UI element that renders the returned {@link UIComponent}, such as a
     *            GWT Widget or an HTMLElement.
     *
     * @return A {@link UIComponent} that with the {@link UIComponent#start(Object, Consumer)}
     *         arguments wired up to the {@link FlowInput} and {@link FlowOutput} instances of the
     *         instances provided by {@code instanceSupplier}.
     */
    public <INPUT, OUTPUT, COMPONENT, T extends COMPONENT> UIComponent<INPUT, OUTPUT, COMPONENT> createUIComponent( final Supplier<T> instanceSupplier,
                                                                                                                    final Consumer<T> starter,
                                                                                                                    final Consumer<T> destroy,
                                                                                                                    final String name ) {
        return new UIComponent<INPUT, OUTPUT, COMPONENT>() {
            T instance;

            T get() {
                if (instance == null) {
                    instance = Factory.maybeUnwrapProxy( instanceSupplier.get() );
                }

                return instance;
            }
            @Override
            public void start( final INPUT input, final Consumer<OUTPUT> callback ) {
                final T instance = get();
                final Frame frame = new Frame( instance, input, callback, t -> {} );
                storeFrame( instance, frame );

                starter.accept( instance );
            }

            @Override
            public void destroy() {
                destroy.accept( get() );
                instance = null;
            }

            @Override
            public COMPONENT asComponent() {
                return get();
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    protected void storeFrame( final Object key, final Frame frame ) {
        if ( frames.containsKey( key ) ) {
            throw new IllegalStateException( "Attempted to store StepFrame for [" + key + "] when one already existed." );
        }
        frames.put( key, frame );
    }

    private Frame removeFrame( final Object key ) {
        final Frame removed = frames.get( key );
        frames.remove( key );
        if ( removed == null ) {
            throw new IllegalStateException( "Attempted to remove StepFrame for [" + key + "] but none existed." );
        }

        return removed;
    }

    private Frame getFrame( final Object key ) {
        return frames.get( key );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void consumeOutput( final Object key, final Object output ) {
        final Frame frame = removeFrame( key );
        ((Consumer) frame.closer).accept( frame.instance );
        ((Consumer) frame.callback).accept( output );
    }

    private Object getInput( final Object key ) {
        return getFrame( key ).input;
    }

    @Produces
    public <T> FlowInput<T> createInput() {
        return new FlowInputImpl<>();
    }

    @Produces
    public <T> FlowOutput<T> createOutput() {
        return new FlowOutputImpl<>();
    }
}
