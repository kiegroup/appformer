package org.dashbuilder.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bundle;

@EntryPoint
@ApplicationScoped
@Bundle("resources/i18n/AppConstants.properties")
public class RuntimeEntryPoint {

    @PostConstruct
    public void hideLoading() {
        Element loading = DomGlobal.document.getElementById("loading");
        if (loading != null) {
            loading.remove();
        }
    }

}