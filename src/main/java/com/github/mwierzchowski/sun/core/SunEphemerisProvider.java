package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
@Service
@RequiredArgsConstructor
public class SunEphemerisProvider {
    public static final String CACHE_NAME = "sun-ephemeris.provider";

    private final SunriseSunsetApi api;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${sun-ephemeris.location.latitude}")
    private Double latitude;

    @Value("${sun-ephemeris.location.longitude}")
    private Double longitude;

    @Retry(name = "SunEphemerisProvider")
    @Cacheable(cacheNames = {CACHE_NAME}, key = "#date.toString()")
    public SunEphemeris sunEphemerisFor(LocalDate date) {
        LOG.info("Requesting sun ephemeris for {}", date);
        var response = apiResponseFor(date);
        LOG.debug("Sunrise-Sunset response: {}", response);
        return sunEphemerisFrom(response.getResults());
    }

    private SunriseSunsetResponse apiResponseFor(LocalDate ephemerisDate) {
        try {
            var result = api.sunriseSunset(latitude, longitude, ephemerisDate.toString(), 0);
            eventPublisher.publishEvent(Status.builder().date(ephemerisDate).build());
            return result;
        } catch (Exception ex) {
            eventPublisher.publishEvent(Status.builder().date(ephemerisDate).exception(ex).build());
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

    @Getter
    @Builder
    public static class Status {
        private LocalDate date;
        private Exception exception;

        public boolean isSuccess() {
            return exception == null;
        }
    }
}
