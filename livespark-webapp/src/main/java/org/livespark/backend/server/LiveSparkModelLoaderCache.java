package org.livespark.backend.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.kie.api.builder.KieModule;
import org.kie.scanner.KieModuleMetaData;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.backend.builder.LRUBuilderCache;
import org.kie.workbench.common.services.shared.project.KieProject;

@ApplicationScoped
public class LiveSparkModelLoaderCache {

	@Inject
	private LRUBuilderCache builderCache;

	@Inject
	private DataModelerService dataModelerService;

	private final Map<Project, ClassLoader> cache = new ConcurrentHashMap<Project, ClassLoader>();

	public ClassLoader getClassLoader(final Project project) {
		return cache.get(project);
	}

	public void setClassLoader(final Project project,
			final ClassLoader classLoader) {
		cache.put(project, classLoader);
	}

	public void initProject(final Project project) {
		final KieModule module = builderCache.assertBuilder(project)
				.getKieModuleIgnoringErrors();
		final ClassLoader classLoader = KieModuleMetaData.Factory
				.newKieModuleMetaData(module).getClassLoader();
		cache.put(project, classLoader);

		if (project instanceof KieProject) {
			dataModelerService.loadModel((KieProject) project);
		}
	}
}
