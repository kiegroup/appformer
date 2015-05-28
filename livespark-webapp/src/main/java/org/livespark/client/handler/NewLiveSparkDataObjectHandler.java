package org.livespark.client.handler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Package;
import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.screens.datamodeller.client.handlers.JavaFileOptions;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.screens.javaeditor.client.resources.JavaEditorResources;
import org.kie.workbench.common.screens.javaeditor.client.type.JavaResourceType;
import org.kie.workbench.common.widgets.client.handlers.DefaultNewResourceHandler;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;
import org.uberfire.workbench.type.ResourceTypeDefinition;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;

@ApplicationScoped
public class NewLiveSparkDataObjectHandler extends DefaultNewResourceHandler {

    @Inject
    private JavaResourceType resourceType;

    @Inject
    private BusyIndicatorView busyIndicatorView;

    @Inject
    private Caller<DataModelerService> dataModelerService;

    @Inject
    private JavaFileOptions options;

    @Override
    public String getDescription() {
        // TODO make constant
        return "LiveSpark DataObject";
    }

    @Override
    public IsWidget getIcon() {
        return new Image( JavaEditorResources.INSTANCE.images().typeJava() );
    }

    @Override
    public ResourceTypeDefinition getResourceType() {
        return resourceType;
    }

    @Override
    public void create( Package pkg,
                        String baseFileName,
                        NewResourcePresenter presenter ) {
        busyIndicatorView.showBusyIndicator( CommonConstants.INSTANCE.Saving() );
        dataModelerService.call( getSuccessCallback( presenter ),
                                 new HasBusyIndicatorDefaultErrorCallback( busyIndicatorView ) )
                          .createJavaFile( idempotentAppend(pkg.getPackageMainSrcPath(), "/client/local"),
                                           buildFileName( baseFileName,
                                                          resourceType ),
                                           "",
                                           options.isPersitable(),
                                           options.getTableName() );

    }

    private Path idempotentAppend( final Path path,
                                   final String toAppend ) {
        if ( path.toURI().endsWith( toAppend ) ) return path;

        return new AppendedPath( toAppend, path );
    }

}
