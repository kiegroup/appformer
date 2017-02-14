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


package org.kie.appformer.formmodeler.rendering.client.flow;

import static java.util.Collections.singletonList;
import static org.jboss.errai.common.client.dom.DOMUtil.removeFromParent;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.api.AppFlowFactory;
import org.kie.appformer.flow.api.Command;
import org.kie.appformer.flow.api.CrudOperation;
import org.kie.appformer.flow.api.Step;
import org.kie.appformer.flow.api.UIComponent;
import org.kie.appformer.flow.api.Unit;
import org.kie.appformer.formmodeler.rendering.client.shared.AppFormerRestService;
import org.kie.appformer.formmodeler.rendering.client.shared.FormModel;
import org.kie.appformer.formmodeler.rendering.client.view.FormView;
import org.kie.appformer.formmodeler.rendering.client.view.ListView;
import org.kie.appformer.formmodeler.rendering.client.view.StandaloneFormWrapper;
import org.kie.appformer.formmodeler.rendering.client.view.UIComponentCleanUpWrapper;
import org.kie.workbench.common.forms.crud.client.component.formDisplay.FormDisplayer.FormDisplayerCallback;
import org.kie.workbench.common.forms.crud.client.component.formDisplay.modal.ModalFormDisplayer;

public abstract class FlowProducer<MODEL,
                                   FORM_MODEL extends FormModel<MODEL>,
                                   FORM_VIEW extends FormView<MODEL, FORM_MODEL>,
                                   LIST_VIEW extends ListView<MODEL, FORM_MODEL>,
                                   REST_SERVICE extends AppFormerRestService<MODEL>> {

    @Inject
    private AppFlowFactory flowFactory;

    @Inject
    private Caller<REST_SERVICE> restService;

    @Inject
    private Event<IsElement> event;

    @Inject
    private ManagedInstance<LIST_VIEW> listViewProvider;

    @Inject
    private ManagedInstance<FORM_VIEW> formViewProvider;

    @Inject
    private ManagedInstance<StandaloneFormWrapper<MODEL, FORM_MODEL, FORM_VIEW>> wrapperProvider;

    @Inject
    private ManagedInstance<ModalFormDisplayer> modalDisplayerProvider;

    public abstract FORM_MODEL modelToFormModel( MODEL model );
    public abstract MODEL formModelToModel( FORM_MODEL formModel );
    public abstract MODEL newModel();
    public abstract Class<MODEL> getModelType();
    public abstract Class<FORM_MODEL> getFormModelType();

    public FORM_MODEL newFormModel() {
        return modelToFormModel( newModel() );
    }

    public Step<MODEL, MODEL> save() {
        return new Step<MODEL, MODEL>() {

            @Override
            public void execute( final MODEL input, final Consumer<MODEL> callback ) {
                restService
                    .call( (final MODEL result) -> callback.accept( result ) )
                    .create( input );
            }

            @Override
            public String getName() {
                return "Save";
            }
        };
    }

    public Step<MODEL, MODEL> update() {
        return new Step<MODEL, MODEL>() {

            @Override
            public void execute( final MODEL input, final Consumer<MODEL> callback ) {
                restService
                    .call( ( final Boolean result ) -> callback.accept( input ) )
                    .update( input );
            }

            @Override
            public String getName() {
                return "Update";
            }
        };
    }

    public Step<MODEL, MODEL> delete() {
        return new Step<MODEL, MODEL>() {

            @Override
            public void execute( final MODEL input, final Consumer<MODEL> callback ) {
                restService
                    .call( result -> callback.accept( input ) )
                    .delete( input );
            }

            @Override
            public String getName() {
                return "Delete";
            }
        };
    }

    public Step<Unit, FlowDataProvider<MODEL>> load() {
        return new Step<Unit, FlowDataProvider<MODEL>>() {

            @Override
            public void execute( final Unit input,
                                 final Consumer<FlowDataProvider<MODEL>> callback ) {
                callback.accept( new RestCallerDataProvider<>( restService ) );
            }

            @Override
            public String getName() {
                return "Load";
            }
        };
    }

    public UIComponent<FORM_MODEL, Optional<FORM_MODEL>, IsElement> standaloneFormView() {
        final StandaloneFormWrapper<MODEL, FORM_MODEL, FORM_VIEW> wrapper = wrapperProvider.get();
        final FORM_VIEW formView = formViewProvider.get();

        return new UIComponent<FORM_MODEL, Optional<FORM_MODEL>, IsElement>() {

            @Override
            public void start( final FORM_MODEL input,
                               final Consumer<Optional<FORM_MODEL>> callback ) {
                formView.setModel( input );
                wrapper.start( formView, oFormView -> callback.accept( oFormView.map( view -> view.getModel() ) ) );
            }

            @Override
            public void onHide() {
                wrapper.onHide();
                formViewProvider.destroy( wrapper.getFormView() );
                wrapperProvider.destroy( wrapper );
            }

            @Override
            public IsElement asComponent() {
                return wrapper;
            }

            @Override
            public String getName() {
                return "Standalone Form View";
            }
        };
    }

    public Step<FORM_MODEL, Optional<FORM_MODEL>> displayModalForm() {
        return new Step<FORM_MODEL, Optional<FORM_MODEL>>() {

            FORM_VIEW view;
            ModalFormDisplayer displayer;

            @Override
            public void execute( final FORM_MODEL input,
                                 final Consumer<Optional<FORM_MODEL>> callback ) {
                view = formViewProvider.get();
                view.setModel( input );
                view.pauseBinding();
                displayer = modalDisplayerProvider.get();
                displayer.display( "Form", view, new FormDisplayerCallback() {

                    @Override
                    public void onCancel() {
                        view.resumeBinding( false );
                        callback.accept( Optional.empty() );
                        modalDisplayerProvider.destroy( displayer );
                        formViewProvider.destroy( view );
                    }

                    @Override
                    public void onAccept() {
                        view.resumeBinding( true );
                        callback.accept( Optional.of( view.getModel() ) );
                        modalDisplayerProvider.destroy( displayer );
                        formViewProvider.destroy( view );
                    }
                } );

            }

            @Override
            public String getName() {
                return "Modal Form";
            }
        };
    }

    public UIComponent<FlowDataProvider<MODEL>, Command<CrudOperation, MODEL>, ListView<MODEL, FORM_MODEL>> listView( final boolean allowCreate,
                                                                                                                       final boolean allowEdit,
                                                                                                                       final boolean allowDelete ) {
        final LIST_VIEW listView = listViewProvider.get();
        listView.setAllowCreate( allowCreate );
        listView.setAllowEdit( allowEdit );
        listView.setAllowDelete( allowDelete );

        return new UIComponentCleanUpWrapper<>( listView, listViewProvider::destroy );
    }

    public <INPUT, OUTPUT> Step<INPUT, OUTPUT> displayMain( final UIComponent<INPUT, OUTPUT, ? extends IsElement> ui ) {
        return new Step<INPUT, OUTPUT>() {
            @Override
            public void execute( final INPUT input,
                                 final Consumer<OUTPUT> callback ) {
                event.fire( ui.asComponent() );
                ui.start( input, output -> callback.accept( output ) );
            }

            @Override
            public String getName() {
                return "Display " + ui.getName();
            }
        };
    }

    public <INPUT, OUTPUT> Step<OUTPUT, OUTPUT> hideMain( final UIComponent<INPUT, OUTPUT, ? extends IsElement> ui ) {
        return new Step<OUTPUT, OUTPUT>() {

            @Override
            public void execute( final OUTPUT input,
                                 final Consumer<OUTPUT> callback ) {
                removeFromParent( ui.asComponent().getElement() );
                ui.onHide();
                callback.accept( input );
            }

            @Override
            public String getName() {
                return "Hide " + ui.getName();
            }
        };
    }

    public AppFlow<FORM_MODEL, Optional<FORM_MODEL>> createOrUpdate( final Supplier<Step<MODEL, MODEL>> persist,
                                                                     final Supplier<AppFlow<FORM_MODEL, Optional<FORM_MODEL>>> formFlow ) {
        return formFlow.get()
                .transitionTo( (final Optional<FORM_MODEL> oModel) ->
                    oModel
                        .map( (final FORM_MODEL model) ->
                            flowFactory
                                .buildFromConstant( model )
                                .andThen( this::formModelToModel )
                                .andThen( persist.get() )
                                .andThen( this::modelToFormModel )
                                .andThen( Optional::of ) )
                        .orElseGet( () ->
                            flowFactory
                                .buildFromConstant( Optional.empty() ) ) );
    }

    public AppFlow<FORM_MODEL, Optional<FORM_MODEL>> displayMainStandaloneForm() {
        return flowFactory
                .buildFromTransition( formModel -> {
                    final UIComponent<FORM_MODEL, Optional<FORM_MODEL>, IsElement> form = standaloneFormView();

                    return flowFactory
                            .buildFromConstant( formModel )
                            .andThen( displayMain( form ) )
                            .andThen( hideMain( form ) );
                } );
    }

    public AppFlow<Unit, Optional<FORM_MODEL>> create() {
        return flowFactory
                .buildFromSupplier( this::newFormModel )
                .andThen( createOrUpdate( this::save, this::displayMainStandaloneForm ) );
    }

    public AppFlow<Unit, Unit> crud() {
        return flowFactory
                .buildFromStep( load() )
                .transitionTo( (final FlowDataProvider<MODEL> initial) -> {
                    final UIComponent<FlowDataProvider<MODEL>, Command<CrudOperation, MODEL>, ListView<MODEL, FORM_MODEL>> listView = listView( true, true, true );
                    return flowFactory
                            .buildFromStep( displayMain( listView ) )
                            .transitionTo( this::crudTransition )
                            .loop( flowFactory,
                                   ( final FlowDataProvider<MODEL> provider,
                                     final Optional<Command<CrudOperation, MODEL>> oExecutedCommand ) ->
                                       oExecutedCommand
                                           .map( executedCommand -> {
                                               if ( executedCommand.commandType.equals( CrudOperation.DELETE ) ) {
                                                   provider.clearCache();
                                               }

                                               return provider;
                                            } )
                                           .map( Optional::of )
                                           .orElseGet( () -> Optional.of( provider ) )
                                   )
                            .withInput( initial )
                            .toUnit();
                } );
        }

    private AppFlow<Unit, Optional<Command<CrudOperation, MODEL>>> crudTransition( final Command<CrudOperation, MODEL> command ) {
        switch ( command.commandType ) {
            case CREATE :
                return flowFactory
                        .buildFromConstant( command.value )
                        .andThen( this::modelToFormModel )
                        .andThen( createOrUpdate( this::save, () -> flowFactory.buildFromStep( displayModalForm() ) ) )
                        .andThen( (final Optional<FORM_MODEL> oFormModel) -> oFormModel.map( this::formModelToModel ) )
                        .andThen( (final Optional<MODEL> oModel) -> oModel.map( model -> new Command<>( command.commandType, model ) ) );

            case UPDATE :
                return flowFactory
                        .buildFromConstant( command.value )
                        .andThen( this::modelToFormModel )
                        .andThen( createOrUpdate( this::update, () -> flowFactory.buildFromStep( displayModalForm() ) ) )
                        .andThen( (final Optional<FORM_MODEL> oFormModel) -> oFormModel.map( this::formModelToModel ) )
                        .andThen( (final Optional<MODEL> oModel) -> oModel.map( model -> new Command<>( command.commandType, model ) ) );
            case DELETE :
                return flowFactory
                        .buildFromConstant( command.value )
                        .andThen( delete() )
                        .andThen( model -> Optional.of( command ) );
            default :
                throw new RuntimeException( "Unrecognized command type " + command.commandType );
        }
    }

    public AppFlow<Unit, Unit> createAndReview() {
        return flowFactory
                .buildFromConstant( newFormModel() )
                .andThen( createOrUpdate( this::save, this::displayMainStandaloneForm ) )
                .transitionTo( oFormModel ->
                        oFormModel
                            .map( this::review )
                            .orElseGet( flowFactory::unitFlow ) );
    }

    public AppFlow<Unit, Unit> review( final FORM_MODEL formModel ) {
        return flowFactory
                .buildFromConstant( new ListAsyncDataProviderAdapter<>( singletonList( formModelToModel( formModel ) ) ) )
                .transitionTo( (final ListAsyncDataProviderAdapter<MODEL> provider) -> {
                    final UIComponent<FlowDataProvider<MODEL>, Command<CrudOperation, MODEL>, ListView<MODEL, FORM_MODEL>> listView = listView( false, true, true );
                    return flowFactory
                        .buildFromConstant( provider )
                        .andThen( displayMain( listView ) )
                        .andThen( hideMain( listView ) );
                } )
                .transitionTo( (final Command<CrudOperation, MODEL> command) -> {
                    switch ( command.commandType ) {
                        case DELETE :
                            return flowFactory
                                    .buildFromConstant( command.value )
                                    .andThen( delete() )
                                    .toUnit();
                        case UPDATE :
                            return flowFactory
                                    .buildFromConstant( modelToFormModel( command.value ) )
                                    .andThen( createOrUpdate( this::update, this::displayMainStandaloneForm ) )
                                    .transitionTo( oUpdatedFormModel ->
                                            oUpdatedFormModel
                                                .map( this::review )
                                                .orElseGet( () -> flowFactory
                                                                    .unitFlow()
                                                                    .andThen( review( modelToFormModel( command.value ) ) ) )
                                            );
                        default :
                            throw new RuntimeException( "Unrecognized command type " + command.commandType );
                    }
                } );
    }

    public AppFlow<Unit, Unit> view() {
        return flowFactory
                .buildFromStep( load() )
                .transitionTo( (final FlowDataProvider<MODEL> provider) -> {
                    final UIComponent<FlowDataProvider<MODEL>, Command<CrudOperation, MODEL>, ListView<MODEL, FORM_MODEL>> listView = listView( false, false, false );
                    return flowFactory
                            .buildFromConstant( provider )
                            .andThen( displayMain( listView ) )
                            .andThen( hideMain( listView ) );
                } )
                .toUnit();
    }

}
