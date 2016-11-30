/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.livespark;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class RestApiJspTest {

    private static final Logger logger = LoggerFactory.getLogger(RestApiJspTest.class);

    // @formatter:off
    private static final Reflections reflections = new Reflections(
            ClasspathHelper.forPackage("org.guvnor.rest"),
            new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new SubTypesScanner());
    // @formatter:on

    private static final Class[] restMethodAnnosArr = { GET.class, POST.class, DELETE.class, PUT.class };

    private static final class RestMethodInfo {
        private Set<String> restPathOperations = new TreeSet<String>();
        private Map<String, Map<String, String>> restUrlParameterDefs = new HashMap<String, Map<String, String>>();

        public Set<String> getRestPathOperations() {
            return restPathOperations;
        }

        public void addRestOperation( String restOperPath, String restOper) {
            this.restPathOperations.add(restOperPath + SPLIT_CHAR + restOper);

        }
        public Map<String, Map<String, String>> getRestUrlParameterDefs() {
            return restUrlParameterDefs;
        }

        public void addRestUrlParameterDefinition(String url, String paramName, String paramRegex) {
            Map<String, String> paramNameRegexMap = restUrlParameterDefs.get(url);
            if( paramNameRegexMap == null ) {
                paramNameRegexMap = new HashMap<String, String>(2);
                restUrlParameterDefs.put(url, paramNameRegexMap);
            }
            String prevRegex = paramNameRegexMap.put(paramName, paramRegex);
            assertTrue( "Regex in url has 2 different definitions [" + url + "]", prevRegex == null || prevRegex.equals(paramRegex));
        }


    }

    private static final Pattern restParamPattern = Pattern.compile("\\{(\\w+): ([^\\/]+)\\}+");

    private RestMethodInfo getRestMethodInfo() throws Exception {
        Set<Class> restMethodAnnos = new HashSet<Class>(Arrays.asList(restMethodAnnosArr));
        Set<Class<?>> resourceImplClasses = reflections.getTypesAnnotatedWith(Path.class);

        Map<Method, String> resourceMethods = new HashMap<Method, String>();
        for( Class resourceImplClass : resourceImplClasses ) {
            for( Method possibleRestMethod : resourceImplClass.getDeclaredMethods() ) {
                for( Annotation anno : possibleRestMethod.getAnnotations() ) {
                    if( restMethodAnnos.contains(anno.annotationType()) ) {
                        resourceMethods.put(possibleRestMethod, anno.annotationType().getSimpleName());
                    }
                }
            }
        }

        RestMethodInfo restInfo = new RestMethodInfo();
        Map<String, String> paramDefinitions = new HashMap<String, String>();
        for( Entry<Method,String> entry : resourceMethods.entrySet() ) {
            Method restMethod = entry.getKey();
            String basePath = restMethod.getDeclaringClass().getAnnotation(Path.class).value();
            if( basePath.endsWith("/") ) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
            Path methodPathAnno = restMethod.getAnnotation(Path.class);
            String methodPath;
            if( methodPathAnno == null ) {
                methodPath = "";
            } else {
                methodPath = methodPathAnno.value();
            }
            String restOperPath = basePath + methodPath;

            Matcher m = restParamPattern.matcher(restOperPath);
            while( m.find() ) {
                String fullParam = m.group();
                assertEquals( "Bug in test: there should be 2 groups!", 2, m.groupCount());
                String paramName = m.group(1);
                String paramRegex = m.group(2);
                restInfo.addRestUrlParameterDefinition(restOperPath, paramName, paramRegex);
                String prevParamRegex = paramDefinitions.put(paramName, paramRegex);
                assertTrue( "Unequal param definition for '" + paramName + "' : [" + paramRegex + "/" + prevParamRegex + "]",
                        prevParamRegex == null || paramRegex.equals(prevParamRegex));
                restOperPath = restOperPath.replace(fullParam, "{" + paramName + "}");
            }
            restInfo.addRestOperation(restOperPath, entry.getValue());
        }

        return restInfo;
    }

    private static final String SPLIT_CHAR = "#";

    @Test
    public void restApiJspCorrectReferencesAllRestOperations() throws Exception {
        Properties testProps = new Properties();
        String testPropFile = "/test.properties";
        InputStream propIn = getClass().getResourceAsStream( "/test.properties" );
        assertNotNull("Unable to find and read " + testPropFile, propIn);
        testProps.load(propIn);

        String baseDir = testProps.getProperty("baseDir");
        String restApiJspPath = baseDir + "/src/main/webapp/rest-api.jsp";
        File restApiJspFile = new File(restApiJspPath);
        assertTrue("rest-api.jsp not found at " + restApiJspPath, restApiJspFile.exists());

        Document restApiJspDoc = Jsoup.parse(restApiJspFile, "UTF-8", "http://localhost:8080/kie-wb/");
        assertNotNull("Could not parse rest-api.jsp with JSoup", restApiJspDoc);

        RestMethodInfo restInfo = getRestMethodInfo();
        Set<String> restPathOperations = restInfo.getRestPathOperations();
        Set<String> jspPaths = new HashSet<String>(restPathOperations.size());

        Set<String> restPaths = new HashSet<String>();
        for( String restPathOper : restInfo.getRestPathOperations() ) {
            String [] restPathInfo = restPathOper.split("#");
            restPaths.add(restPathInfo[0]);
//            System.out.println( "  <tr><td>" + restPathInfo[0] + "</td><td>" + restPathInfo[1] + "</td><td>descr</td></tr>");
        }
        Map<String, String> jspParams = new TreeMap<String, String>();
        Map<String, String> paramDefs = new TreeMap<String, String>();
        for( Map<String, String> paramDef : restInfo.getRestUrlParameterDefs().values() ) {
            for( Entry<String, String> entry : paramDef.entrySet() ) {
                String prevValue = paramDefs.put(entry.getKey(), entry.getValue());
                if( prevValue != null ) {
                    assertEquals ( "Different values for parameter " + entry.getKey(), prevValue, entry.getValue() );
                }
            }
        }
        for( Entry<String, String> paramEntry : paramDefs.entrySet() ) {
//            System.out.println( "  <tr><td><code>" + paramEntry.getKey() + "</code></td><td><code>" + paramEntry.getValue() + "</code></td></tr>");
        }

        Elements tables = restApiJspDoc.body().getElementsByTag("table");
        assertFalse(tables.isEmpty());
        for( Element table : tables ) {
            Elements rows = table.getElementsByTag("tr");
            for( Element row : rows ) {
                String idAttr = table.attr("id");
                if( "parameters".equals(idAttr) ) {
                    String [] paramDef = new String[2];
                    for( Element cell : row.getElementsByTag("td") ) {
                        String text = cell.text();
                        if( text.startsWith("[") ) {
                            // regex
                           paramDef[1] = text;
                        } else if( ! text.contains(" ") ) {
                            // param name
                            paramDef[0] = text;
                        }
                        if( paramDef[1] != null && paramDef[1] != null ) {
                            jspParams.put(paramDef[0], paramDef[1]);
                            assertTrue( "Unknown param definition: [" + paramDef[0] + "]", paramDefs.containsKey(paramDef[0]));
                            assertEquals( "Incorrect param definition: [" + paramDef[0] + "]",
                                    paramDefs.get(paramDef[0]), paramDef[1] );
                        }
                    }
                } else {
                    String [] restPathOp = new String[2];
                    for( Element cell : row.getElementsByTag("td") ) {
                        String text = cell.text();
                        if( text.startsWith("/" ) ) {
                            // url
                            jspPaths.add(text);
                            restPathOp[1] = text;
                        } else if( text.matches("[A-Z]+")) {
                            // HTTP operatoin
                            restPathOp[0] = text;
                        }
                    }
                    if( restPathOp[1] != null ) {
                        assertNotNull( "No HTTP operation provided for [" + restPathOp[1] + "]", restPathOp[0]);
                        assertTrue( "[" + restPathOp[0] + "] is an incorrect REST operation path for " + restPathOp[1], restPaths.contains(restPathOp[1]));
                        assertTrue( "Incorrect HTTP operation for [" + restPathOp[1] + "]",
                                restInfo.getRestPathOperations().contains(restPathOp[1] + SPLIT_CHAR + restPathOp[0]));
                    }
                }
            }
        }

        for( String restPath : restPaths ) {
            assertTrue( "[" + restPath + "] is not shown in the JSP.", jspPaths.contains(restPath) );
        }
        for( Entry<String, String> entry : paramDefs.entrySet() ) {
            assertTrue( "Parameter [" + entry.getKey() + "] is not definied in the JSP.", jspParams.containsKey(entry.getKey()));
            assertEquals( "Parameter [" + entry.getKey() + "] is not correctly defined in the JSP.", entry.getValue(), jspParams.get(entry.getKey()));
        }
    }
}
