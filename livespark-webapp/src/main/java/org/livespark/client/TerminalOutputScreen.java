/*
 * Copyright 2014 JBoss Inc
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
package org.livespark.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.uberfire.ext.widgets.common.client.ace.AceEditor;
import org.uberfire.ext.widgets.common.client.ace.AceEditorMode;
import org.uberfire.ext.widgets.common.client.ace.AceEditorTheme;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;

@Dependent
public class TerminalOutputScreen extends Composite {

    private AceEditor editor = new AceEditor();

    @PostConstruct
    public void setup() {
        initWidget( editor );

        final Style s = getElement().getStyle();
        s.setPosition( Style.Position.ABSOLUTE );
        s.setOverflow( Style.Overflow.HIDDEN );
        s.setTop( 0.0, Style.Unit.PX );
        s.setLeft( 0.0, Style.Unit.PX );
        s.setWidth( 100.0, Style.Unit.PCT );
        s.setHeight( 100.0, Style.Unit.PCT );

        editor.startEditor();
        editor.setTheme( AceEditorTheme.CLOUD9_NIGHT );
    }

    @Override
    public void onAttach() {
        super.onAttach();
        final Style s = editor.getElement().getParentElement().getStyle();
        s.setPosition( Style.Position.ABSOLUTE );
        s.setOverflow( Style.Overflow.HIDDEN );
        s.setTop( 0.0, Style.Unit.PX );
        s.setLeft( 0.0, Style.Unit.PX );
        s.setWidth( 100.0, Style.Unit.PCT );
        s.setHeight( 100.0, Style.Unit.PCT );
    }

    public void setContent( final AceEditorMode mode,
                            final String content ) {
        if ( mode != null ) {
            editor.setMode( mode );
        }

        editor.setText( content );
    }

    public void appendContent( final AceEditorMode mode,
                               final String content ) {
        if ( mode != null ) {
            editor.setMode( mode );
        }

        editor.setText( editor.getText() + content );
    }

    public String getContent() {
        return editor.getText();
    }

    public void setContentAndScroll( final String content ) {
        editor.setText( content );
        editor.scrollToLine( editor.getText().split( "\n" ).length );
    }

    public void appendContentAndScroll( final String content ) {
        editor.setText( editor.getText() + content );
        editor.scrollToLine( editor.getText().split( "\n" ).length );
    }

    public void setWrapMode( boolean wrap ) {
        editor.setUseWrapMode( wrap );
    }

    public void setReadOnly( boolean readOnly ) {
        editor.setReadOnly( readOnly );
    }

}