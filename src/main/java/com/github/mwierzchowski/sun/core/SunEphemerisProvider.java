package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class SunEphemerisProvider {
    public static final String CACHE_NAME = "sun-ephemeris.provider";

    private final SunriseSunsetApi api;
    private final Environment env;

    @Retry(name = "SunEphemerisProvider")
    @Cacheable(cacheNames = {CACHE_NAME}, key = "#date.toString()")
    public SunEphemeris sunEphemerisFor(LocalDate date) {
        LOG.info("Requesting sun ephemeris for {}", date);
        var latitude = env.getRequiredProperty("sun-ephemeris.location.latitude", Double.class);
        var longitude = env.getRequiredProperty("sun-ephemeris.location.longitude", Double.class);
        var response = api.sunriseSunset(latitude, longitude, date.toString(), 0);
        LOG.debug("Sunrise-Sunset response: {}", response);
        return sunEphemerisFrom(response.getResults());
    }

    private SunEphemeris sunEphemerisFrom(SunriseSunsetResponseResults results) {
        var sunEphemeris = new SunEphemeris();
        sunEphemeris.add(DAWN, results.getCivilTwilightBegin());
        sunEphemeris.add(SUNRISE, results.getSunrise());
        sunEphemeris.add(NOON, results.getSolarNoon());
        sunEphemeris.add(SUNSET, results.getSunset());
        sunEphemeris.add(DUSK, results.getCivilTwilightEnd());
        return sunEphemeris;
    }
}
