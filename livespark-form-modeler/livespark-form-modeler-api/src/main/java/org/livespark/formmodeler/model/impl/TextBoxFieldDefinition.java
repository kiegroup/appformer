package org.livespark.formmodeler.model.impl;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
public class TextBoxFieldDefinition extends AbstractIntputFieldDefinition {

    @Override
    public String getStandaloneClassName() {
        return String.class.getName();
    }
}
