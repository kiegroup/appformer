package org.dashbuilder.renderer.c3.client.charts.map.geojson;

import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.renderer.c3.client.jsbinding.geojson.FeatureCollection;

@ApplicationScoped
public interface GeoJsonLoader {
    
    public FeatureCollection load();

}