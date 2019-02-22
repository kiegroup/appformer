package org.dashbuilder.renderer.c3.client.charts.map;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.dashbuilder.renderer.c3.client.charts.map.geojson.CountriesGeoJsonService;

public class D3MapConf {
    
    private String title;
    private Map<String, Double> data;
    private boolean markers;
    private boolean regions;
    private String backgroundColor = "#DDDDFF";
    private CountriesGeoJsonService countriesGeoJsonService;
    private Function<Double, String> formatter;
    private Consumer<String> pathClickHandler;
    
    
    public D3MapConf(String title, Map<String, Double> data, boolean markers, boolean regions, String backgroundColor,
                                  CountriesGeoJsonService countriesGeoJsonService, Function<Double, String> formatter,  Consumer<String> pathClickHandler) {
        this.title = title;
        this.data = data;
        this.markers = markers;
        this.regions = regions;
        this.backgroundColor = backgroundColor;
        this.formatter = formatter;
        this.countriesGeoJsonService = countriesGeoJsonService;
        this.pathClickHandler = pathClickHandler;
    }

    public static D3MapConf of(String title, 
                              Map<String, Double> data, 
                              boolean markers, 
                              boolean regions,
                              String backgroundColor,
                              CountriesGeoJsonService countriesGeoJsonService,
                              Function<Double, String> formatter,
                              Consumer<String> pathClickHandler) {
        return new D3MapConf(title, data, markers, regions, backgroundColor, countriesGeoJsonService, formatter, pathClickHandler);
    }

    public boolean isMarkers() {
        return markers;
    }

    public boolean isRegions() {
        return regions;
    }

    public Map<String, Double> getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public Function<Double, String> getFormatter() {
        return formatter;
    }

    public CountriesGeoJsonService getCountriesGeoJsonService() {
        return countriesGeoJsonService;
    }

    public Consumer<String> getPathClickHandler() {
        return pathClickHandler;
    }
    
}