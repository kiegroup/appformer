/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.client.screens.todo;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import elemental2.dom.DomGlobal;
import org.jboss.errai.common.client.api.Caller;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.shared.Foo;
import org.uberfire.shared.TestEvent;
import org.uberfire.shared.TestMessagesService;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@WorkbenchScreen(identifier = "TodoListScreen", preferredWidth = 400)
public class TodoListScreen extends AbstractMarkdownScreen {

    @Inject
    private Caller<TestMessagesService> testMessagesService;

    @Override
    public String getMarkdownFileURI() {
        return "default://uf-playground/todo.md";
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Todo List";
    }

    public void observeTestEvent(final @Observes TestEvent testEvent) {
        DomGlobal.console.info("Test event received on GWT component!");
    }

    @OnOpen
    public void onOpen() {
        TestEvent testEvent = new TestEvent();
        testEvent.bar = "bar-todolist";
        testEvent.foo = new Foo();
        testEvent.foo.foo = "foo-todolist";
        testEvent.child = new TestEvent();
        testEvent.child.bar = "bar2-todolist";
        testEvent.child.child = null;
        testEvent.child.foo = new Foo();
        testEvent.child.foo.foo = "foo2-todolist";

        testMessagesService.call(a -> {
            DomGlobal.console.info(a);
        }).postTestEvent(testEvent);
    }

    @WorkbenchMenu
    public Menus getMenu() {
        return MenuFactory
                .newTopLevelMenu("Save")
                .respondsWith(() -> Window.alert("Saved!"))
                .endMenu()
                .newTopLevelMenu("Delete")
                .respondsWith(() -> Window.alert("Deleted!"))
                .endMenu()
                .newTopLevelMenu("Edit")
                .menus()
                .menu("Cut")
                .respondsWith(() -> Window.alert("Cut!"))
                .endMenu()
                .menu("Paste")
                .respondsWith(() -> Window.alert("Paste!"))
                .endMenu()
                .endMenus()
                .endMenu()
                .build();
    }
}