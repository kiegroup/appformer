package org.dashbuilder.renderer.c3.client.charts.map.geojson;

import java.util.Map;
import java.util.Optional;

import org.dashbuilder.renderer.c3.client.jsbinding.geojson.Feature;

/**
 * Class for handling GeoJson files that contains country information 
 */
public interface CountriesGeoJsonService {
    
    public Feature[] getCountries();
    
    public String getCountryName(Feature country);
    
    public String getCountryNameByCode(String code);
    
    public Optional<Feature> countryByIdOrName(String idOrName);
    
    public Optional<Map.Entry<String, Double>> entryByCountry(Map<String, Double> data, Feature value);
    
    public Optional<Double> valueByCountry(Map<String, Double> data, Feature value);

}