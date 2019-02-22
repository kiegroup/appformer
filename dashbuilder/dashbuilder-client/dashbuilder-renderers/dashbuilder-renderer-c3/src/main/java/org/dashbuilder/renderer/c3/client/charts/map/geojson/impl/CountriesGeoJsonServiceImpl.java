package org.dashbuilder.renderer.c3.client.charts.map.geojson.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dashbuilder.renderer.c3.client.charts.map.geojson.CountriesGeoJsonService;
import org.dashbuilder.renderer.c3.client.charts.map.geojson.GeoJsonLoader;
import org.dashbuilder.renderer.c3.client.jsbinding.geojson.Feature;
import org.dashbuilder.renderer.c3.client.jsbinding.geojson.FeatureCollection;

public class CountriesGeoJsonServiceImpl implements CountriesGeoJsonService {
    
    private final static String COUNTRY_NAME_PROPERTY = "name";
    
    @Inject
    GeoJsonLoader geoJsonLoader;
    private FeatureCollection featureCollection;
    
    @PostConstruct
    public void setup() {
        featureCollection = geoJsonLoader.load();
    }

    @Override
    public Feature[] getCountries() {
        return featureCollection.getFeatures();
    }
    
    @Override
    public String getCountryName(Feature country) {
        Object name = country.getProperties().get(COUNTRY_NAME_PROPERTY);
        if (name != null) {
            return name.toString();
        }
        return "";
    }

    @Override
    public String getCountryNameByCode(String code) {
        return Arrays.stream(featureCollection.getFeatures())
                  .filter(f -> code.equalsIgnoreCase(f.getId()))
                  .map(this::getCountryName)
                  .findFirst().orElse("");
    }

    @Override
    public Optional<Feature> countryByIdOrName(String idOrName) {
        return Arrays.stream(featureCollection.getFeatures())
                .filter(f -> { 
                    String countryName = getCountryName(f);
                    return idOrName.equalsIgnoreCase(f.getId()) || 
                               idOrName.equalsIgnoreCase(countryName);
                })
                .findFirst();
    }

    @Override
    public Optional<Entry<String, Double>> entryByCountry(Map<String, Double> data, Feature value) {
        String countryID = value.getId();
        String countryName = getCountryName(value);
        
        return data.entrySet().stream().filter(k -> 
                                        k.getKey().equalsIgnoreCase(countryID) || 
                                        k.getKey().equalsIgnoreCase(countryName))
                                    .findFirst();
    }

    @Override
    public Optional<Double> valueByCountry(Map<String, Double> data, Feature value) {
        Optional<Entry<String, Double>> entry = entryByCountry(data, value);
        if (entry.isPresent()) {
            Double val = entry.get().getValue();
            return Optional.ofNullable(val);
        }
        return Optional.empty();
    }

}
