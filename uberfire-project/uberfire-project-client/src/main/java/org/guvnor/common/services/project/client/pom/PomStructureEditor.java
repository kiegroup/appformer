package org.guvnor.common.services.project.client.pom;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.guvnor.structure.pom.AddPomDependencyEvent;
import org.guvnor.structure.pom.DynamicPomDependency;

@ApplicationScoped
public class PomStructureEditor {


    public void onNewDependency(final @Observes AddPomDependencyEvent event) {
        Optional<DynamicPomDependency> dependency = event.getNewPomDependency();

        //PomStructureContextChangeHandler handler : handlers
        /*for (final GuvnorStructureContextChangeHandler handler : handlers.values()) {
            final Optional<Branch> branchOptional = event.getRepository().getBranch(event.getNewBranchName());
            if (branchOptional.isPresent()) {
                handler.onNewBranchAdded(event.getRepository().getAlias(),
                                         event.getNewBranchName(),
                                         branchOptional.get().getPath());
            }
        }*/

    }
}

