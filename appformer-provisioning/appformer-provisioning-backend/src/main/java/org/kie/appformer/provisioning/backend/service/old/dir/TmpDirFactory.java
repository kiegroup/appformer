/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.appformer.provisioning.backend.service.old.dir;

import java.io.File;
import java.io.IOException;

import javax.enterprise.context.Dependent;
import javax.servlet.http.HttpSession;

import org.guvnor.common.services.project.model.Project;

@Dependent
public class TmpDirFactory {

    private static final String TMP_DIR_HANDLE_SESSION_ATTR_KEY = TmpDirHandle.class.getCanonicalName();

    public File getTmpDir( final Project project, final HttpSession session ) throws IOException {
        TmpDirHandle handle = (TmpDirHandle) session.getAttribute( TMP_DIR_HANDLE_SESSION_ATTR_KEY );

        if (handle == null) {
            handle = createNewDirAndHandle( project, session );
            session.setAttribute( TMP_DIR_HANDLE_SESSION_ATTR_KEY, handle );
        }

        return handle.getFile();
    }

    private TmpDirHandle createNewDirAndHandle( final Project project, final HttpSession session ) throws IOException {
        final File tmpDir = createTmpProjectDir( project, session );
        final TmpDirHandle handle = new TmpDirHandle( tmpDir );

        return handle;
    }

    private File createTmpProjectDir( final Project project, final HttpSession session ) throws IOException {
        final File tmpDir = File.createTempFile( project.getProjectName() + session.getId(), "" );
        tmpDir.delete();
        tmpDir.mkdir();

        return tmpDir;
    }

}
