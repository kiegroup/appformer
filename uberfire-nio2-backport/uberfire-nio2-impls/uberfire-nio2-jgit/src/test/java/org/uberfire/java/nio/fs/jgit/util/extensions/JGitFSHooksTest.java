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

package org.uberfire.java.nio.fs.jgit.util.extensions;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.uberfire.java.nio.file.extensions.FileSystemHookExecutionContext;
import org.uberfire.java.nio.file.extensions.FileSystemHooks;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class JGitFSHooksTest {

    @Test
    public void executeFSHooksTest() {

        final String fsName = "dora";

        AtomicBoolean executedWithLambda = new AtomicBoolean(false);

        FileSystemHooks.FileSystemHook hook = spy(new FileSystemHooks.FileSystemHook() {
            @Override
            public void execute(FileSystemHookExecutionContext context) {
                assertEquals(fsName, context.getFsName());
            }
        });

        FileSystemHooks.FileSystemHook lambdaHook = context -> {
            assertEquals(fsName, context.getFsName());
            executedWithLambda.set(true);
        };

        JGitFSHooks.executeFSHooks(hook, FileSystemHooks.ExternalUpdate, new FileSystemHookExecutionContext(fsName));
        JGitFSHooks.executeFSHooks(lambdaHook, FileSystemHooks.ExternalUpdate, new FileSystemHookExecutionContext(fsName));

        verify(hook).execute(any());

        assertTrue(executedWithLambda.get());
    }

    @Test
    public void executeFSHooksArrayTest() {

        final String fsName = "dora";

        AtomicBoolean executedWithLambda = new AtomicBoolean(false);

        FileSystemHooks.FileSystemHook hook = spy(new FileSystemHooks.FileSystemHook() {
            @Override
            public void execute(FileSystemHookExecutionContext context) {
                assertEquals(fsName, context.getFsName());
            }
        });

        FileSystemHooks.FileSystemHook lambdaHook = context -> {
            assertEquals(fsName, context.getFsName());
            executedWithLambda.set(true);
        };

        JGitFSHooks.executeFSHooks(Arrays.asList(hook, lambdaHook), FileSystemHooks.ExternalUpdate, new FileSystemHookExecutionContext(fsName));

        verify(hook).execute(any());
        assertTrue(executedWithLambda.get());
    }
}