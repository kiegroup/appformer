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

package org.uberfire.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.Path;
import org.uberfire.server.util.FileServletUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileUploadServletTest {

    //Parameters expected by the FileUploadServlet.
    private static final String PARAM_PATH = "path";
    private static final String PARAM_FOLDER = "folder";
    private static final String PARAM_FILENAME = "fileName";
    private static final String PARAM_UPDATE = "update";

    private static final String TEST_ROOT_PATH = "default://main@test-repository/test-project/src/main/resources/test";
    private static final String TEST_ROOT_PATH_WITH_SPACES = "default://main@mtest-repository/my test project/src/main/resources/test";

    private static final String BOUNDARY = "---------------------------9051914041544843365972754266";
    private static final String BOUNDARY_DELIMITER = "--";
    private static final String CONTENT_TYPE = "multipart/form-data; boundary=\"" + BOUNDARY + "\"";
    /**
     * The Carriage Return ASCII character value.
     */
    private static final byte CR = 0x0D;

    /**
     * The Line Feed ASCII character value.
     */
    private static final byte LF = 0x0A;

    private static final String BREAK = new String(new char[]{CR, LF});

    @Mock
    private IOService ioService;

    @Mock
    private Path path;

    @Mock
    private FileSystem fileSystem;

    @InjectMocks
    private FileUploadServlet uploadServlet;

    @Before
    public void setup() {
        when(ioService.get(any(URI.class))).thenReturn(path);
        when(path.getFileSystem()).thenReturn(fileSystem);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination folder on the server side.
     * 2) a destination file name (with blank spaces).
     * @throws Exception
     */
    @Test
    public void uploadByNameWithSpacesAndFolder() throws Exception {

        //test the upload of a file name with blank spaces into a given folder.
        String targetFileName = "File Name With Spaces.some extension";
        String fileContent = "the local file content";

        doUploadTestByNameAndFolder(targetFileName,
                                    TEST_ROOT_PATH,
                                    fileContent);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination folder on the server side (with blank spaces).
     * 2) a destination file name (with blank spaces).
     * @throws Exception
     */
    @Test
    public void uploadByNameAndFolderWithSpaces() throws Exception {

        //test the upload of a file name with blank spaces into a given folder.
        String targetFileName = "File Name With Spaces.some extension";
        String fileContent = "the local file content";

        doUploadTestByNameAndFolder(targetFileName,
                                    TEST_ROOT_PATH_WITH_SPACES,
                                    fileContent);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination folder on the server side.
     * 2) a destination file name (with NO blank spaces).
     * @throws Exception
     */
    @Test
    public void uploadByNameWithNoSpacesAndFolder() throws Exception {

        //test the upload of a file name with NO blank spaces into a given folder.
        String targetFileName = "FileNameWithNoSpaces.someextension";
        String fileContent = "the local file content";

        doUploadTestByNameAndFolder(targetFileName,
                                    TEST_ROOT_PATH,
                                    fileContent);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination folder on the server side (with blank spaces).
     * 2) a destination file name (with NO blank spaces).
     * @throws Exception
     */
    @Test
    public void uploadByNameWithNoSpacesAndFolderWithSpaces() throws Exception {

        //test the upload of a file name with NO blank spaces into a given folder.
        String targetFileName = "FileNameWithNoSpaces.someextension";
        String fileContent = "the local file content";

        doUploadTestByNameAndFolder(targetFileName,
                                    TEST_ROOT_PATH_WITH_SPACES,
                                    fileContent);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination path, composed of a folder and a file name with blank spaces.
     * @throws Exception
     */
    @Test
    public void uploadByPathWithSpacesAndFolderNoSpaces() throws Exception {

        //test the upload of a file name with blank spaces into a given folder.
        String targetPath = TEST_ROOT_PATH + "/" + "File Name With Spaces.some extension";
        String fileContent = "the local file content";

        doUploadTestByPath(targetPath,
                           fileContent,
                           false,
                           false);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination path, composed of a folder with blank spaces and a file name with blank spaces.
     * @throws Exception
     */
    @Test
    public void uploadByPathWithSpacesAndFolderWithSpaces() throws Exception {

        //test the upload of a file name with blank spaces into a given folder.
        String targetPathWithSpaces = TEST_ROOT_PATH_WITH_SPACES.replaceAll("\\s", "%20") + "/" + "File Name With Spaces.some extension";
        String fileContent = "the local file content";

        doUploadTestByPath(targetPathWithSpaces,
                           fileContent,
                           true,
                           true);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination path, composed of a folder and a file name with no blank spaces.
     * @throws Exception
     */
    @Test
    public void uploadByPathWithNoSpacesAndFolderWithNoSpaces() throws Exception {

        //test the upload of a file name with blank spaces into a given folder.
        String targetPath = TEST_ROOT_PATH.replaceAll("\\s", "%20") + "/" + "FileNameWithNoSpaces.someextension";
        String fileContent = "the local file content";

        doUploadTestByPath(targetPath,
                           fileContent,
                           false,
                           true);
    }
    
    /**
     * Tests the upload failure of a file given the following parameters:
     * <p>
     * 1) a destination path, composed of a folder and a file name with no blank spaces.
     * @throws Exception
     */
    @Test
    public void failedUploadByPathWithNoSpacesAndFolderWithNoSpaces() throws Exception {
        //test the upload of a file name with blank spaces into a given folder.
        String targetPath = TEST_ROOT_PATH.replaceAll("\\s", "%20") + "/" + "FileNameWithNoSpaces.someextension";
        String fileContent = "the local file content";
    
        doUploadTestByPath(targetPath,
                               fileContent,
                               true,
                               false);
    }

    /**
     * Tests the uploading of a file given the following parameters:
     * <p>
     * 1) a destination path, composed of a folder and a file name with no blank spaces.
     * @throws Exception
     */
    @Test
    public void uploadByPathWithNoSpacesAndFolderWithSpaces() throws Exception {

        //test the upload of a file name with blank spaces into a given folder.
        String targetPathWithSpaces = TEST_ROOT_PATH_WITH_SPACES.replaceAll("\\s", "%20") + "/" + "FileNameWithNoSpaces.someextension";
        String fileContent = "the local file content";
        doUploadTestByPath(targetPathWithSpaces,
                           fileContent,
                           false,
                           false);
    }

    private void doUploadTestByNameAndFolder(String targetFileName,
                                             String targetFolderName,
                                             String fileContent) throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String localFileName = "local_file_name.txt"; //not relevant for the test

        //mock the servlet parameters
        when(request.getParameter(PARAM_FOLDER)).thenReturn(targetFolderName);
        when(request.getParameter(PARAM_FILENAME)).thenReturn(targetFileName);

        //mock the servlet multipart request
        //local file name, and local file name content are not relevant
        String requestContent = mockMultipartRequestContent(localFileName,
                                                            fileContent);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestContent.getBytes());
        MockServletInputStream servletInputStream = new MockServletInputStream(inputStream);

        when(request.getContentLength()).thenReturn(requestContent.getBytes().length);
        when(request.getContentType()).thenReturn(CONTENT_TYPE);
        when(request.getInputStream()).thenReturn(servletInputStream);

        //mock the servlet response writer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        when(response.getWriter()).thenReturn(printWriter);

        //FileUploadServlet uploadServlet = new FileUploadServlet();
        uploadServlet.doPost(request,
                             response);

        verify(request,
               times(1)).getParameter(PARAM_PATH);
        verify(request,
               times(2)).getParameter(PARAM_FOLDER);
        verify(request,
               times(1)).getParameter(PARAM_FILENAME);

        //Expected URI
        URI expectedURI = new URI(targetFolderName.replaceAll("\\s", "%20") + "/" + FileServletUtil.encodeFileName(targetFileName));

        verify(ioService,
               times(2)).startBatch(eq(fileSystem));
        verify(ioService,
               times(1)).get(eq(expectedURI));
        verify(ioService,
               times(1)).write(any(Path.class),
                               eq(fileContent.getBytes()));
        verify(ioService,
               times(2)).endBatch();

        printWriter.flush();
        assertEquals("OK",
                     new String(outputStream.toByteArray()));
    }

    private void doUploadTestByPath(String targetPath,
                                    String fileContent,
                                    boolean fileExists,
                                    boolean isUpdate) throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String localFileName = "local_file_name.txt"; //not relevant for the test

        //mock the servlet parameters
        when(request.getParameter(PARAM_PATH)).thenReturn(targetPath);
        when(request.getParameter(PARAM_UPDATE)).thenReturn(String.valueOf(isUpdate));
        
        when(ioService.exists(any(Path.class))).thenReturn(fileExists);

        //mock the servlet multipart request
        //local file name, and local file name content are not relevant
        String requestContent = mockMultipartRequestContent(localFileName,
                                                            fileContent);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestContent.getBytes());
        MockServletInputStream servletInputStream = new MockServletInputStream(inputStream);

        when(request.getContentLength()).thenReturn(requestContent.getBytes().length);
        when(request.getContentType()).thenReturn(CONTENT_TYPE);
        when(request.getInputStream()).thenReturn(servletInputStream);

        //mock the servlet response writer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        when(response.getWriter()).thenReturn(printWriter);

        uploadServlet.doPost(request,
                             response);

        verify(request,
               times(2)).getParameter(PARAM_PATH);
        verify(request, times(1)).getParameter(PARAM_UPDATE);
    
        //Expected URI
        URI expectedURI = new URI(FileServletUtil.encodeFileNamePart(targetPath));
        verify(ioService,
               times(1)).get(eq(expectedURI));
    
        if(fileExists && !isUpdate) {
            verify(ioService,
                   times(1)).startBatch(eq(fileSystem));
            verify(ioService, times(1)).exists(any(Path.class));
            verify(ioService,
                   times(1)).endBatch();
    
            printWriter.flush();
            assertEquals("CONFLICT",
                         new String(outputStream.toByteArray()));
        } else {
            verify(ioService,
                   times(2)).startBatch(eq(fileSystem));
            
            verify(ioService,
                   times(1)).write(any(Path.class),
                                   eq(fileContent.getBytes()));
            verify(ioService,
                   times(2)).endBatch();
        
            printWriter.flush();
            assertEquals("OK",
                         new String(outputStream.toByteArray()));
        }
    }

    private String mockMultipartRequestContent(String localFileName,
                                               String fileContent) {
        String content = BOUNDARY_DELIMITER + BOUNDARY + BREAK +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + localFileName + "\"" + BREAK +
                "Content-Type: text/plain" + BREAK + BREAK +

                fileContent + BREAK +
                BOUNDARY_DELIMITER + BOUNDARY + BOUNDARY_DELIMITER + BREAK;
        return content;
    }

    private class MockServletInputStream extends ServletInputStream {

        InputStream content;

        public MockServletInputStream(InputStream content) {
            this.content = content;
        }

        @Override
        public int read() throws IOException {
            return content.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return content.read(b);
        }

        @Override
        public int read(byte[] b,
                        int off,
                        int len) throws IOException {
            return content.read(b,
                                off,
                                len);
        }

        @Override
        public long skip(long n) throws IOException {
            return content.skip(n);
        }

        @Override
        public int available() throws IOException {
            return content.available();
        }

        @Override
        public void close() throws IOException {
            content.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            content.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            content.reset();
        }

        @Override
        public boolean markSupported() {
            return content.markSupported();
        }

        @Override
        public boolean isFinished() {
            try {
                return content.available() <= 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // TODO how to treat the listener?
        }
    }
}