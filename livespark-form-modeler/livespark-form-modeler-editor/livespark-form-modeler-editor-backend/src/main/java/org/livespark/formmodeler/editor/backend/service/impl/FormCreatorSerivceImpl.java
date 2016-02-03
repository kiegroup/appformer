package org.livespark.formmodeler.editor.backend.service.impl;

import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;
import org.livespark.formmodeler.editor.service.FormCreatorService;
import org.livespark.formmodeler.model.FormDefinition;

/**
 * Created by pefernan on 9/18/15.
 */
@Service
@ApplicationScoped
public class FormCreatorSerivceImpl implements FormCreatorService {

    @Override
    public FormDefinition getNewFormInstance() {
        FormDefinition form = new FormDefinition();

        form.setId( UUID.randomUUID().toString() );

        return form;
    }
}
