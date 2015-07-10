package org.livespark.backend.server.service.build;

import java.util.List;
import java.util.concurrent.Callable;

import org.guvnor.common.services.project.builder.model.BuildMessage;


public interface BuildCallable extends Callable<List<BuildMessage>> {
}
