package org.dashbuilder.renderer.c3.client.exports;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface NativeLibraryResources extends ClientBundle {

    NativeLibraryResources INSTANCE = GWT.create(NativeLibraryResources.class);

    @Source("js/c3.min.js")
    TextResource c3js();

    @Source("js/d3-geo-projection.min.js")
    TextResource d3geoprojectionjs();    
    
    @Source("json/countries.geo.json")
    TextResource countriesgeojson();    

}