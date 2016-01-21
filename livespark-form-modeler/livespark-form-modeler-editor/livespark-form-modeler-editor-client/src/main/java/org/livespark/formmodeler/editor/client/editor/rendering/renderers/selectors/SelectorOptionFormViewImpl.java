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

package org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors;

import java.util.List;
import javax.enterprise.context.Dependent;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.livespark.formmodeler.editor.client.resources.i18n.Constants;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorOption;
import org.uberfire.ext.properties.editor.client.widgets.AbstractPropertyEditorWidget;
import org.uberfire.ext.widgets.common.client.common.CheckboxCellImpl;

@Dependent
public class SelectorOptionFormViewImpl extends AbstractPropertyEditorWidget implements SelectorOptionFormPresenter.SelectorOptionFormView {

    interface SelectorOptionFormViewBinder
            extends UiBinder<Widget, SelectorOptionFormViewImpl> {

    }

    private static SelectorOptionFormViewBinder uiBinder = GWT.create( SelectorOptionFormViewBinder.class );

    @UiField
    FormGroup optionGroup;

    @UiField
    TextBox option;

    @UiField
    HelpBlock optionHelp;

    @UiField
    FormGroup optionTextGroup;

    @UiField
    TextBox optionText;

    @UiField
    HelpBlock optionTextHelp;

    @UiField
    Button newOption;

    @UiField
    CellTable<SelectorOption> optionsTable;

    private ListDataProvider<SelectorOption> optionsListDataProvider = new ListDataProvider<SelectorOption>();

    protected SelectorOptionFormPresenter presenter;

    public SelectorOptionFormViewImpl() {
        initWidget(uiBinder.createAndBindUi(SelectorOptionFormViewImpl.this));

        //Init data objects table
        optionsTable.setEmptyTableWidget(new Label(FieldProperties.INSTANCE.noOptionsDefined()));
        optionsListDataProvider.addDataDisplay(optionsTable);

        CheckboxCellImpl defaultValue = new CheckboxCellImpl( false );

        final Column<SelectorOption, Boolean> defaultValueColumn = new Column<SelectorOption, Boolean>( defaultValue ) {
            @Override
            public Boolean getValue( SelectorOption object ) {
                return object.getDefaultValue();
            }
        };

        defaultValueColumn.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
        defaultValueColumn.setFieldUpdater( new FieldUpdater<SelectorOption, Boolean>() {
            @Override
            public void update( int index, SelectorOption option, Boolean value ) {
                presenter.setDefaultValue( option );
            }
        } );

        optionsTable.addColumn( defaultValueColumn, FieldProperties.INSTANCE.defaultOption() );
        optionsTable.setColumnWidth( defaultValueColumn, 30, Style.Unit.PCT );

        final TextColumn<SelectorOption> valueColumn = new TextColumn<SelectorOption>() {

            @Override
            public void render( Cell.Context context,
                                SelectorOption object,
                                SafeHtmlBuilder sb ) {
                SafeHtml startDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "<div style=\"cursor: pointer;\">";
                    }
                };
                SafeHtml endDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "</div>";
                    }
                };

                sb.append( startDiv );
                super.render( context, object, sb );
                sb.append( endDiv );
            }

            @Override
            public String getValue( final SelectorOption option ) {
                return option.getValue();
            }
        };

        optionsTable.addColumn( valueColumn, FieldProperties.INSTANCE.option() );

        final TextColumn<SelectorOption> textColumn = new TextColumn<SelectorOption>() {

            @Override
            public void render( Cell.Context context,
                                SelectorOption object,
                                SafeHtmlBuilder sb ) {
                SafeHtml startDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "<div style=\"cursor: pointer;\">";
                    }
                };
                SafeHtml endDiv = new SafeHtml() {
                    @Override
                    public String asString() {
                        return "</div>";
                    }
                };

                sb.append( startDiv );
                super.render( context, object, sb );
                sb.append( endDiv );
            }

            @Override
            public String getValue( final SelectorOption option ) {
                return option.getText();
            }
        };

        optionsTable.addColumn( textColumn, FieldProperties.INSTANCE.optionText() );

        final ButtonCell deleteCell = new ButtonCell( ButtonType.DANGER, IconType.TRASH );
        final Column<SelectorOption, String> deleteOptionColumn = new Column<SelectorOption, String>( deleteCell ) {
            @Override
            public String getValue( final SelectorOption option ) {
                return Constants.INSTANCE.remove();
            }
        };

        deleteOptionColumn.setFieldUpdater( new FieldUpdater<SelectorOption, String>() {
            public void update( final int index,
                                final SelectorOption option,
                                final String value ) {
                presenter.removeOption( option );
            }
        } );

        deleteOptionColumn.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
        optionsTable.addColumn( deleteOptionColumn );
    }

    @Override
    public void setPresenter( SelectorOptionFormPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setOptions( List<SelectorOption> options ) {
        optionsListDataProvider.setList(options);
        optionsListDataProvider.refresh();
    }

    @UiHandler("newOption")
    public void addNewOption( ClickEvent event ) {
        if ( validate() ) {
            presenter.addOption( option.getText(), optionText.getText() );
            option.clear();
            optionText.clear();
            resetFormValidations();
        }
    }

    protected boolean validate() {
        resetFormValidations();
        if ( option.getText().isEmpty() ) {
            seOptiontError( FieldProperties.INSTANCE.optionCannotBeEmpty() );
            return false;
        }
        if ( presenter.existOption( option.getText() ) ) {
            seOptiontError( FieldProperties.INSTANCE.optionAlreadyExist() );
            return false;
        }
        if ( presenter.existOptionText( optionText.getText() ) ) {
            optionTextGroup.setValidationState( ValidationState.ERROR );
            optionTextHelp.setIconType( IconType.WARNING );
            optionTextHelp.setText( FieldProperties.INSTANCE.optionAlreadyExist() );
            return false;
        }

        return true;
    }

    protected void seOptiontError( String message ) {
        optionGroup.setValidationState( ValidationState.ERROR );
        optionHelp.setIconType( IconType.WARNING );
        optionHelp.setText( message );
    }

    protected void resetFormValidations() {
        optionHelp.setIconType( null );
        optionHelp.setText( "" );
        optionGroup.setValidationState( ValidationState.NONE );

        optionTextHelp.setIconType( null );
        optionTextHelp.setText( "" );
        optionTextGroup.setValidationState( ValidationState.NONE );
    }
}
