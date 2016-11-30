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
package org.livespark.formmodeler.codegen.util;

public interface SourceGenerationUtil {

    public static final String GWT_DOM_CLASSNAME = "com.google.gwt.user.client.DOM";

    public static final String GWT_SAFE_HTML_UTILS_NAME = "SafeHtmlUtils";
    public static final String GWT_SAFE_HTML_UTILS_CLASSNAME = "com.google.gwt.safehtml.shared." + GWT_SAFE_HTML_UTILS_NAME;
    public static final String GWT_SAFE_HTML_UTILS_FROM_TRUSTED_SOURCE = GWT_SAFE_HTML_UTILS_NAME + ".fromTrustedString( \"&nbsp;\" )";

    public static final String JAVA_LANG_OVERRIDE = "java.lang.Override";

    public static final String ERRAI_PORTABLE = "org.jboss.errai.common.client.api.annotations.Portable";
    public static final String ERRAI_BINDABLE = "org.jboss.errai.databinding.client.api.Bindable";
    public static final String ERRAI_BOUND = "org.jboss.errai.ui.shared.api.annotations.Bound";

    public static final String ERRAI_MAPS_TO = "org.jboss.errai.common.client.api.annotations.MapsTo";
    public static final String ERRAI_DATAFIELD = "org.jboss.errai.ui.shared.api.annotations.DataField";
    public static final String ERRAI_TEMPLATED = "org.jboss.errai.ui.shared.api.annotations.Templated";

    public static final String ERRAI_REST_CLIENT = "org.jboss.errai.enterprise.client.jaxrs.api.RestClient";
    public static final String ERRAI_REMOTE_CALLBACK = "org.jboss.errai.common.client.api.RemoteCallback";

    public static final String INJECT_NAMED = "javax.inject.Named";
    public static final String INJECT_INJECT = "javax.inject.Inject";

    public static final String VALIDATION_VALID = "javax.validation.Valid";
    public static final String VALIDATION_NOT_NULL = "javax.validation.constraints.NotNull";
    public static final String HIBERNATE_NOT_EMPTY = "org.hibernate.validator.constraints.NotEmpty";

    public static final String EJB_STATELESS = "javax.ejb.Stateless";
    public static final String EJB_ENTITY_MANAGER = "javax.persistence.EntityManager";
    public static final String EJB_PERSISTENCE_CONTEXT = "javax.persistence.PersistenceContext";
    public static final String EJB_TRANSACTION_ATTR = "javax.ejb.TransactionAttribute";
    public static final String EJB_REQUIRES_NEW = "javax.ejb.TransactionAttributeType.REQUIRES_NEW";

    public static final String FORM_MODEL_CLASS = "org.livespark.formmodeler.rendering.client.shared.FormModel";
    public static final String FORM_VIEW_CLASS = "org.livespark.formmodeler.rendering.client.view.FormView";
    public static final String LIST_VIEW_CLASS = "org.livespark.formmodeler.rendering.client.view.ListView";

    public static final String COLUMN_META_CLASS_NAME = "org.uberfire.ext.widgets.table.client.ColumnMeta";
    public static final String COLUMN_METAS_VAR_NAME = "columnMetas";
    public static final String COLUMN_META_SUFFIX = "_columnMeta";

    public static final String LIST_VIEW_DELETE_EXECUTOR = "org.livespark.formmodeler.rendering.client.view.ListView.DeleteExecutor";

    public static final String ENTITY_SERVICE_CLASS = "org.livespark.formmodeler.rendering.server.rest.BaseEntityService";

    public static final String BASE_REST_SERVICE = "org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService";

    public static final String READONLY_PARAM = "readOnly";

    public static final String JAVAX_PERSISTENCE_ID = "javax.persistence.Id";

    public static final String SUBFORM_ADAPTER_SUFFIX = "SubFormModelAdapter";
    public static final String SUBFORM_ClASSNAME = "org.livespark.formmodeler.rendering.client.shared.fields.SubFormModelAdapter";

    public static final String MULTIPLE_SUBFORM_ADAPTER_SUFFIX = "MultipleSubFormModelAdapter";
    public static final String MULTIPLE_SUBFORM_ClASSNAME = "org.livespark.formmodeler.rendering.client.shared.fields.MultipleSubFormModelAdapter";

    public static final String JAVA_UTIL_LIST_CLASSNAME = "java.util.List";
    public static final String JAVA_UTIL_ARRAYLIST_CLASSNAME = "java.util.ArrayList";

    public static final String JAVA_UTIL_MAP_CLASSNAME = "java.util.Map";
    public static final String JAVA_UTIL_MAP_NAME = "Map";
    public static final String JAVA_UTIL_HASHMAP_CLASSNAME = "java.util.HashMap";
    public static final String JAVA_UTIL_HASHMAP_NAME = "HashMap";

    public static final String IS_NEW_MODEL_METHOD_CALL = "isNewModel()";
    public static final String INIT_FORM_METHOD = "initForm";
    public static final String BEFORE_DISPLAY_METHOD = "beforeDisplay";
    public static final String DO_EXTRA_VALIDATIONS_METHOD = "doExtraValidations";
    public static final String SET_MODEL_METHOD = "setModel";
}
