package org.vaadin.alump.gridstack.demo.openweathermap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WeatherResponse {
    private final List<OpenWeatherMapEntry> weathers;

    public WeatherResponse(Collection<OpenWeatherMapEntry> weathers) {
        this.weathers = Collections.unmodifiableList(new ArrayList<>(weathers));
    }

    public Collection<OpenWeatherMapEntry> getWeathers() {
        return weathers;
    }
}
