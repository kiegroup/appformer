package org.dashbuilder.renderer.c3.client;

import org.dashbuilder.renderer.c3.client.jsbinding.C3;
import org.dashbuilder.renderer.c3.client.jsbinding.C3Chart;
import org.dashbuilder.renderer.c3.client.jsbinding.C3ChartConf;
import org.dashbuilder.renderer.c3.client.resources.i18n.C3DisplayerConstants;
import org.dashbuilder.renderer.c3.mutationobserver.MutationObserverFactory;

import elemental2.dom.DomGlobal;
import elemental2.dom.MutationObserver;
import elemental2.dom.MutationObserverInit;
import elemental2.dom.Node;
import jsinterop.base.Js;

public abstract class C3DisplayerView<P extends C3Displayer> 
        extends C3AbstractDisplayerView<P> 
        implements C3Displayer.View<P> {
    
    protected C3Chart chart;
    
    @Override
    public void init(P presenter) {
        super.init(presenter);
    }

    @Override
    public void updateChart(C3ChartConf conf) {
        displayerPanel.clear();
        conf.setBindto(displayerPanel.getElement());
        chart = C3.generate(conf);
    }
    
    @Override
    public String getGroupsTitle() {
        return C3DisplayerConstants.INSTANCE.common_Categories();
    }
    
    @Override
    public String getColumnsTitle() {
        return C3DisplayerConstants.INSTANCE.common_Series();
    }
    
    @Override
    public void setBackgroundColor(String color) {
        chart.getElement().getElementsByTagName("svg")
                          .getItem(0).getStyle()
                          .setBackgroundColor(color);
    }
    
    
    public void setResizable(int maxWidth, int maxHeight) {
        displayerPanel.setWidth("100%");
        displayerPanel.getElement().getStyle().setProperty("maxWidth", maxWidth + "px");
        displayerPanel.getElement().getStyle().setProperty("maxHeight", maxHeight + "px");
        registerMutationObserver();
    }
    
    private void registerMutationObserver() {
        MutationObserver observer = new MutationObserver((records, obs) ->  {
            Node elementalNode = Js.cast(displayerPanel.getElement());
            if (DomGlobal.document.body.contains((elementalNode))) {
                if (chart != null) {
                    chart.flush();
                }
                obs.disconnect();
            }
            return null;
        });
        MutationObserverInit options = new MutationObserverFactory().mutationObserverInit();
        options.childList = true;
        observer.observe(DomGlobal.document.body, options);
    }
    
}
