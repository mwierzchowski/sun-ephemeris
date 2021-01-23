package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.sunrisesunset.api.SunriseSunsetApi;
import org.sunrisesunset.model.SunriseSunsetResponseResults;

import java.time.LocalDate;

import static com.github.mwierzchowski.sun.core.SunEventType.DAWN;
import static com.github.mwierzchowski.sun.core.SunEventType.DUSK;
import static com.github.mwierzchowski.sun.core.SunEventType.NOON;
import static com.github.mwierzchowski.sun.core.SunEventType.SUNRISE;
import static com.github.mwierzchowski.sun.core.SunEventType.SUNSET;

@Slf4j
@Service
@CacheConfig(cacheNames = {"sun-ephemeris-calculator"})
@RequiredArgsConstructor
public class SunEphemerisCalculator {
    private final SunriseSunsetApi api;
    private final Environment env;

    @Cacheable
    @Retry(name = "sun-ephemeris-calculator")
    public SunEphemeris sunEphemerisFor(LocalDate date) {
        var strDate = date.toString();
        var latitude = env.getRequiredProperty("sun-ephemeris.location.latitude", Double.class);
        var longitude = env.getRequiredProperty("sun-ephemeris.location.longitude", Double.class);
        LOG.debug("Requesting sun ephemeris for date={}, lat={}, lon={}", strDate, latitude, longitude);
        var response = api.sunriseSunset(latitude, longitude, strDate, 0);
        LOG.debug("Sunrise-Sunset response: {}", response);
        return sunEphemerisOf(response.getResults());
    }

    // TODO cache evict after few days

    private SunEphemeris sunEphemerisOf(SunriseSunsetResponseResults results) {
        var sunEphemeris = new SunEphemeris();
        sunEphemeris.add(DAWN, results.getCivilTwilightBegin());
        sunEphemeris.add(SUNRISE, results.getSunrise());
        sunEphemeris.add(NOON, results.getSolarNoon());
        sunEphemeris.add(SUNSET, results.getSunset());
        sunEphemeris.add(DUSK, results.getCivilTwilightEnd());
        return sunEphemeris;
    }
}
