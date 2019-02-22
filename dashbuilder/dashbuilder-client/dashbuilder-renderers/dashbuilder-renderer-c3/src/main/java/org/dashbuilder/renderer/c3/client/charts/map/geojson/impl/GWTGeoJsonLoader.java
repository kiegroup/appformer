package org.dashbuilder.renderer.c3.client.charts.map.geojson.impl;

import org.dashbuilder.renderer.c3.client.charts.map.geojson.GeoJsonLoader;
import org.dashbuilder.renderer.c3.client.exports.NativeLibraryResources;
import org.dashbuilder.renderer.c3.client.jsbinding.geojson.FeatureCollection;

import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import jsinterop.base.Js;

public class GWTGeoJsonLoader implements GeoJsonLoader {

    @Override
    public FeatureCollection load() {
        String geoJsonStr = NativeLibraryResources.INSTANCE.countriesgeojson().getText();
        JSONValue geoJsonObject = JSONParser.parseStrict(geoJsonStr);
        return Js.cast(geoJsonObject.isObject().getJavaScriptObject());
    }

}
