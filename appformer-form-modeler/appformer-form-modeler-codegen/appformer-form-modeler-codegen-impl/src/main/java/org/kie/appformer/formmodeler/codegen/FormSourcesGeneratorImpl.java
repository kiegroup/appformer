/*
 * Copyright 2015 JBoss Inc
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

package org.kie.appformer.formmodeler.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.model.Package;
import org.kie.appformer.formmodeler.codegen.flow.FlowLangSourceGenerator;
import org.kie.appformer.formmodeler.codegen.flow.FlowProducer;
import org.kie.appformer.formmodeler.codegen.model.FormModel;
import org.kie.appformer.formmodeler.codegen.rest.EntityService;
import org.kie.appformer.formmodeler.codegen.rest.RestApi;
import org.kie.appformer.formmodeler.codegen.rest.RestImpl;
import org.kie.appformer.formmodeler.codegen.view.FormView;
import org.kie.appformer.formmodeler.codegen.view.HTMLTemplateGenerator;
import org.kie.appformer.formmodeler.codegen.view.ListView;
import org.kie.workbench.common.forms.commons.shared.layout.FormLayoutTemplateGenerator;
import org.kie.workbench.common.forms.commons.shared.layout.Static;
import org.kie.workbench.common.forms.editor.service.shared.VFSFormFinderService;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.serialization.FormDefinitionSerializer;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;

@ApplicationScoped
public class FormSourcesGeneratorImpl implements FormSourcesGenerator {

    private static transient Logger log = LoggerFactory.getLogger(FormSourcesGeneratorImpl.class);

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private DataModelerService dataModelerService;

    @Inject
    @FormModel
    private JavaSourceGenerator formModelSourceGenerator;

    @Inject
    @FormView
    private JavaSourceGenerator formViewSourceGenerator;

    @Inject
    @FormView
    private HTMLTemplateGenerator formViewTemplateGenerator;

    @Inject
    private FormDefinitionSerializer formDefinitionSerializer;

    @Inject
    @Static
    private FormLayoutTemplateGenerator formLayoutTemplateGenerator;

    @Inject
    @ListView
    private JavaSourceGenerator listViewSourceGenerator;

    @Inject
    @ListView
    private HTMLTemplateGenerator listViewTemplateGenerator;

    @Inject
    @RestApi
    private JavaSourceGenerator restApiSourceGenerator;

    @Inject
    @EntityService
    private JavaSourceGenerator entityServiceSourceGenerator;

    @Inject
    @RestImpl
    private JavaSourceGenerator restImplSourceGenerator;

    @Inject
    @FlowProducer
    private JavaSourceGenerator flowProducerSourceGenerator;

    @Inject
    private FlowLangSourceGenerator mainFlowGenerator;

    @Inject
    private ErraiAppPropertiesGenerator serializableTypesGenerator;

    @Inject
    private VFSFormFinderService vfsFormFinderService;

    @Inject
    private CommentedOptionFactory commentedOptionFactory;

    @Override
    public void generateEntityFormSources(final FormDefinition form,
                                          final Path resourcePath) {
        final Package resPackage = projectService.resolvePackage(resourcePath);
        final KieProject project = projectService.resolveProject(resourcePath);

        final Package root = getRootPackage(resPackage);

        final Package client = getOrCreateClientPackage(root);
        final Package local = getOrCreateLocalPackage(client);
        final Package shared = getOrCreateSharedPackage(client);
        final Package server = getOrCreateServerPackage(root);

        final SourceGenerationContext context = new SourceGenerationContext(form,
                                                                            resourcePath,
                                                                            root,
                                                                            local,
                                                                            shared,
                                                                            server,
                                                                            vfsFormFinderService.findAllForms(resourcePath));

        final String formModelSource = formModelSourceGenerator.generateJavaSource(context);

        if (form.getLayoutTemplate() == null) {
            formLayoutTemplateGenerator.generateLayoutTemplate(form);
        }

        final String formTemplateLayout = formDefinitionSerializer.serialize(form);

        final String formViewSource = formViewSourceGenerator.generateJavaSource(context);
        final String formViewTemplate = formViewTemplateGenerator.generateHTMLTemplate(context);

        final String listViewSource = listViewSourceGenerator.generateJavaSource(context);
        final String listViewTemplate = listViewTemplateGenerator.generateHTMLTemplate(context);

        final String restApiSource = restApiSourceGenerator.generateJavaSource(context);
        final String restImplSource = restImplSourceGenerator.generateJavaSource(context);
        final String entityServiceSource = entityServiceSourceGenerator.generateJavaSource(context);

        final String flowProducerSource = flowProducerSourceGenerator.generateJavaSource(context);

        final String mainFlowSource = mainFlowGenerator.generateInitialFlowSource(context);

        final String serializableTypesDeclaration = serializableTypesGenerator.generate(getSerializableTypeClassNames(project));

        if (!allNonEmpty(resourcePath,
                         formModelSource,
                         formTemplateLayout,
                         formViewSource,
                         formViewTemplate,
                         listViewSource,
                         listViewTemplate,
                         restApiSource,
                         restImplSource,
                         entityServiceSource,
                         flowProducerSource,
                         mainFlowSource,
                         serializableTypesDeclaration)) {
            log.warn("Unable to generate the required form assets for Data Object: {}",
                     resourcePath);
            return;
        }

        final org.uberfire.java.nio.file.Path parent = Paths.convert(resourcePath).getParent();

        ioService.startBatch(parent.getFileSystem());
        try {
            writeJavaSource(resourcePath,
                            context.getFormModelName(),
                            formModelSource,
                            shared);
            writeFormTemplate(resourcePath,
                              form.getName(),
                              formTemplateLayout,
                              shared);

            writeJavaSource(resourcePath,
                            context.getFormViewName(),
                            formViewSource,
                            local);
            writeJavaSource(resourcePath,
                            context.getListViewName(),
                            listViewSource,
                            local);
            writeJavaSource(resourcePath,
                            context.getFlowProducerName(),
                            flowProducerSource,
                            local);
            writeJavaSource(resourcePath,
                            context.getRestServiceName(),
                            restApiSource,
                            shared);
            writeJavaSource(resourcePath,
                            context.getRestServiceImplName(),
                            restImplSource,
                            server);
            writeJavaSource(resourcePath,
                            context.getEntityServiceName(),
                            entityServiceSource,
                            server);

            writeHTMLSource(resourcePath,
                            context.getFormViewName(),
                            formViewTemplate,
                            local);
            writeHTMLSource(resourcePath,
                            context.getListViewName(),
                            listViewTemplate,
                            local);

            writeErraiAppProperties(serializableTypesDeclaration,
                                    project);

            maybeWriteFlowSource(resourcePath,
                                 "Main",
                                 mainFlowSource,
                                 local);
            maybeUpdateFlowSourceImports(resourcePath,
                                         "Main",
                                         local,
                                         context);
        } catch (final Exception e) {
            log.error("It was not possible to generate form sources for file: " + resourcePath + " due to the following errors.",
                      e);
        } finally {
            ioService.endBatch();
        }
    }

    @Override
    public void generateFormSources(final FormDefinition form,
                                    final Path resourcePath) {
        final Package resPackage = projectService.resolvePackage(resourcePath);

        final Package root = getRootPackage(resPackage);

        final Package client = getOrCreateClientPackage(root);
        final Package local = getOrCreateLocalPackage(client);
        final Package shared = getOrCreateSharedPackage(client);

        final SourceGenerationContext context = new SourceGenerationContext(form,
                                                                            resourcePath,
                                                                            root,
                                                                            local,
                                                                            shared,
                                                                            null,
                                                                            vfsFormFinderService.findAllForms(resourcePath));

        final String modelSource = formModelSourceGenerator.generateJavaSource(context);

        final String javaTemplate = formViewSourceGenerator.generateJavaSource(context);
        final String htmlTemplate = formViewTemplateGenerator.generateHTMLTemplate(context);

        if (!allNonEmpty(resourcePath,
                         modelSource,
                         javaTemplate,
                         htmlTemplate)) {
            log.warn("Unable to generate the required form assets for Data Object: {}",
                     resourcePath);
            return;
        }

        final org.uberfire.java.nio.file.Path parent = Paths.convert(resourcePath).getParent();

        ioService.startBatch(parent.getFileSystem());
        try {
            writeJavaSource(resourcePath,
                            context.getFormModelName(),
                            modelSource,
                            shared);
            writeJavaSource(resourcePath,
                            context.getFormViewName(),
                            javaTemplate,
                            local);
            writeHTMLSource(resourcePath,
                            context.getFormViewName(),
                            htmlTemplate,
                            local);
        } catch (final Exception e) {
            log.error("It was not possible to generate form sources for file: " + resourcePath + " due to the following errors.",
                      e);
        } finally {
            ioService.endBatch();
        }
    }

    private Collection<String> getSerializableTypeClassNames(final KieProject project) {
        final Set<DataObject> dataObjects = dataModelerService.loadModel(project).getDataObjects();
        final Collection<String> retVal = new ArrayList<>(dataObjects.size());

        for (final DataObject dataObject : dataObjects) {
            final String className = dataObject.getClassName();
            if (isNotDerivedObject(className)) {
                retVal.add(className);
            }
        }

        return retVal;
    }

    private boolean isNotDerivedObject(final String className) {
        // TODO figure out a less hacky implementation of this method
        return !(className.endsWith(SourceGenerationContext.ENTITY_SERVICE_SUFFIX)
                || className.endsWith(SourceGenerationContext.FORM_MODEL_SUFFIX)
                || className.endsWith(SourceGenerationContext.FORM_VIEW_SUFFIX)
                || className.endsWith(SourceGenerationContext.LIST_VIEW_SUFFIX)
                || className.endsWith(SourceGenerationContext.REST_IMPL_SUFFIX)
                || className.endsWith(SourceGenerationContext.REST_SERVICE_SUFFIX)
                || className.contains(".builtin.")
                || className.contains(".server.")
                || className.contains(".backend."));
    }

    private void writeErraiAppProperties(final String serializableTypesDeclaration,
                                         final KieProject project) {
        final Package defaultPackage = projectService.resolveDefaultPackage(project);
        final Path resourceRoot = defaultPackage.getPackageMainResourcesPath();

        final org.uberfire.java.nio.file.Path parentPath = Paths.convert(resourceRoot);
        final org.uberfire.java.nio.file.Path filePath = parentPath.resolve("ErraiApp.properties");
        ioService.write(filePath,
                        serializableTypesDeclaration,
                        makeCommentedOption("Updated ErraiApp.properties."));
    }

    private Package getOrCreateServerPackage(final Package root) {
        return getOrCreateSubpackage(root,
                                     "server");
    }

    private Package getOrCreateSharedPackage(final Package client) {
        return getOrCreateSubpackage(client,
                                     "shared");
    }

    private Package getOrCreateLocalPackage(final Package client) {
        return getOrCreateSubpackage(client,
                                     "local");
    }

    private Package getOrCreateClientPackage(final Package root) {
        return getOrCreateSubpackage(root,
                                     "client");
    }

    private Package getOrCreateSubpackage(final Package root,
                                          final String subPackage) {
        final Path fullPath = PathFactory.newPath("/",
                                                  root.getPackageMainSrcPath().toURI() + "/" + subPackage);
        Package resolved = projectService.resolvePackage(fullPath);

        if (resolved == null) {
            resolved = projectService.newPackage(root,
                                                 subPackage);
        }

        return resolved;
    }

    private Package getRootPackage(final Package resPackage) {
        if (!resPackage.getPackageName().endsWith("client.shared")) {
            return resPackage;
        }

        Package cur = resPackage;
        while (cur.getPackageName().matches(".*\\.client(\\..*)?")) {
            cur = projectService.resolveParentPackage(cur);
        }

        return cur;
    }

    private boolean allNonEmpty(final Path resourcePath,
                                final String... templates) {
        for (final String template : templates) {
            if (StringUtils.isEmpty(template)) {
                return false;
            }
        }

        return true;
    }

    private void writeFormTemplate(final Path dataObjectPath,
                                   final String name,
                                   final String formTemplate,
                                   final Package sourcePackage) {
        final org.uberfire.java.nio.file.Path parentPath = Paths.convert(sourcePackage.getPackageMainResourcesPath());
        final org.uberfire.java.nio.file.Path htmlPath = parentPath.resolve(name + ".frm");

        ioService.write(htmlPath,
                        formTemplate,
                        makeCommentedOption("Added HTML Source for Form Template '" + dataObjectPath + "'"));
    }

    private void writeHTMLSource(final Path dataObjectPath,
                                 final String name,
                                 final String htmlTemplate,
                                 final Package sourcePackage) {
        final org.uberfire.java.nio.file.Path parentPath = Paths.convert(sourcePackage.getPackageMainResourcesPath());
        final org.uberfire.java.nio.file.Path htmlPath = parentPath.resolve(name + ".html");

        ioService.write(htmlPath,
                        htmlTemplate,
                        makeCommentedOption("Added HTML Source for Form Template '" + dataObjectPath + "'"));
    }

    private void maybeWriteFlowSource(final Path dataObjectPath,
                                      final String name,
                                      final String flowFileTemplate,
                                      final Package sourcePackage) {
        final org.uberfire.java.nio.file.Path parentPath = Paths.convert(sourcePackage.getProjectRootPath()).resolve("src/main/resources");
        final org.uberfire.java.nio.file.Path flowPath = parentPath.resolve(name + ".flow");

        final boolean flowFileExists = ioService.exists(flowPath);

        if (!flowFileExists) {
            ioService.write(flowPath,
                            flowFileTemplate,
                            makeCommentedOption("Added Flow Source for Form Template '" + dataObjectPath + "'"));
        }
    }

    private void maybeUpdateFlowSourceImports(final Path dataObjectPath,
                                              final String name,
                                              final Package sourcePackage,
                                              final SourceGenerationContext context) {
        final org.uberfire.java.nio.file.Path parentPath = Paths.convert(sourcePackage.getProjectRootPath()).resolve("src/main/resources");
        final org.uberfire.java.nio.file.Path flowPath = parentPath.resolve(name + ".flow");

        final String originalFlowSource = ioService.readAllString(flowPath);

        mainFlowGenerator
                .updateSource(context,
                              originalFlowSource)
                .ifPresent(newSrc -> {
                    ioService.write(flowPath,
                                    newSrc,
                                    makeCommentedOption("Updated Flow Source imports for Form Template '" + dataObjectPath + "'"));
                });
    }

    private void writeJavaSource(final Path dataObjectPath,
                                 final String name,
                                 final String javaSource,
                                 final Package sourcePackage) {
        final org.uberfire.java.nio.file.Path parentPath = Paths.convert(sourcePackage.getPackageMainSrcPath());
        final org.uberfire.java.nio.file.Path filePath = parentPath.resolve(name + ".java");
        ioService.write(filePath,
                        javaSource,
                        makeCommentedOption("Added Java Source for Form Model '" + dataObjectPath + "'"));
    }

    public CommentedOption makeCommentedOption(final String commitMessage) {
        return commentedOptionFactory.makeCommentedOption(commitMessage);
    }
}
