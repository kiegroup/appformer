package org.dashbuilder.renderer.c3.client.charts.map;

import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.renderer.c3.client.exports.NativeLibraryResources;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

@ApplicationScoped
public class GeoData {
    
    private JSONArray countries;

    @PostConstruct
    public void loadGeoJson() {
        String jsonString = NativeLibraryResources.INSTANCE.countriesgeojson().getText();
        countries = JSONParser.parseStrict(jsonString).isObject().get("features").isArray();
    }
    
    public JSONArray countries() {
        return countries;
    }
    
    public  String countryName(JSONValue value) {
        return value.isObject().get("properties").isObject().get("name").toString().replaceAll("\"", "");
    }
    
    public String countryID(JSONValue value) {
        return value.isObject().get("id").toString().replaceAll("\"", "");
    }
    
    public Optional<JSONValue> findPathByCountryIdOrName(String idOrName) {
        JSONValue result = null;
        for (int i = 0; i < countries.size(); i++) {
            JSONValue value = countries.get(i);
            if (idOrName.equalsIgnoreCase(countryID(value)) || 
                idOrName.equalsIgnoreCase(countryName(value))) {
                result = value;
                break;
            }
        }
        return Optional.ofNullable(result);
    }
    
    public Optional<Map.Entry<String, Double>> entryByCountry(Map<String, Double> data, JSONValue value) {
        String countryID = countryID(value);
        String countryName = countryName(value);
        
        return data.entrySet().stream().filter(k -> 
                                        k.getKey().equalsIgnoreCase(countryID) || 
                                        k.getKey().equalsIgnoreCase(countryName))
                                    .findFirst();
   }

    public Optional<Double> valueByCountry(Map<String, Double> data, JSONValue value) {
        Optional<Entry<String, Double>> entry = entryByCountry(data, value);
        if (entry.isPresent()) {
            Double val = entry.get().getValue();
            return Optional.ofNullable(val);
        }
        return Optional.empty();
    }
    

}
