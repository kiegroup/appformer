/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.dashbuilder.dsl.serialization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import org.dashbuilder.dsl.model.Dashboard;
import org.dashbuilder.dsl.serialization.impl.DashboardZipSerializer;
import org.dashbuilder.dsl.validation.DashboardValidator;
import org.dashbuilder.dsl.validation.ValidationResult;
import org.dashbuilder.dsl.validation.ValidationResult.ValidationResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardExporter {

    private static final Logger logger = LoggerFactory.getLogger(DashboardExporter.class);
    private static final DashboardExporter INSTANCE = new DashboardExporter();

    DashboardValidator validator = DashboardValidator.get();

    public enum ExportType {
        ZIP
    }

    private DashboardExporter() {
        // empty
    }

    public static DashboardExporter get() {
        return INSTANCE;
    }

    public void export(Dashboard dashboard, String path, ExportType type) {
        DashboardSerializer serializer = serializerFor(type);
        validate(dashboard);
        try (FileOutputStream fos = new FileOutputStream(path)) {
            serializer.serialize(dashboard, fos);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found: " + path);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file " + path);
        }
    }

    void validate(Dashboard dashboard) {
        List<ValidationResult> results = validator.validate(dashboard);
        
        printResult(results, ValidationResultType.ERROR, logger::error);
        printResult(results, ValidationResultType.WARNING, logger::warn);
        printResult(results, ValidationResultType.SUCCESS, logger::info);

        if (results.stream().anyMatch(p -> p.getType() == ValidationResultType.ERROR)) {
            throw new IllegalArgumentException("There are validation errors, check logs for more details");
        }

    }

    private void printResult(List<ValidationResult> results, ValidationResultType type, Consumer<String> printer) {
        results.stream()
               .filter(v -> v.getType() == type)
               .map(Object::toString)
               .forEach(printer::accept);
    }

    private static DashboardZipSerializer serializerFor(ExportType type) {
        // only ZIP us supported at the moment
        return new DashboardZipSerializer();
    }

}