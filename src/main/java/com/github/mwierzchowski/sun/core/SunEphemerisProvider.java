package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.sunrisesunset.api.SunriseSunsetApi;
import org.sunrisesunset.model.SunriseSunsetResponse;
import org.sunrisesunset.model.SunriseSunsetResponseResults;

import java.time.LocalDate;

import static com.github.mwierzchowski.sun.core.SunEventType.DAWN;
import static com.github.mwierzchowski.sun.core.SunEventType.DUSK;
import static com.github.mwierzchowski.sun.core.SunEventType.NOON;
import static com.github.mwierzchowski.sun.core.SunEventType.SUNRISE;
import static com.github.mwierzchowski.sun.core.SunEventType.SUNSET;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class SunEphemerisProvider {
    public static final String CACHE = "sun-ephemeris:provider:cache";

    private final SunriseSunsetApi api;
    private final ApplicationEventPublisher publisher;

    @Value("${location.latitude}")
    private Double latitude;

    @Value("${location.longitude}")
    private Double longitude;

    @Retry(name = "SunEphemerisProvider")
    @Cacheable(cacheNames = CACHE, key = "#date.toString()")
    public SunEphemeris sunEphemerisFor(LocalDate date) {
        LOG.info("Requesting sun ephemeris for {}", date);
        try {
            var response = api.sunriseSunset(latitude, longitude, date.toString(), 0);
            LOG.debug("Sunrise-Sunset response: {}", response);
            publisher.publishEvent(new SuccessEvent(response));
            return sunEphemerisFrom(response.getResults());
        } catch (Exception ex) {
            publisher.publishEvent(new FailureEvent(ex));
            throw ex;
        }
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

    public static class SuccessEvent extends ApplicationEvent {
        SuccessEvent(SunriseSunsetResponse response) {
            super(response);
        }
    }

    public static class FailureEvent extends ApplicationEvent {
        FailureEvent(Exception exception) {
            super(exception);
        }
    }
}
