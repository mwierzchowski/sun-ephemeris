package com.github.mwierzchowski.sun.management;

import com.github.mwierzchowski.sun.core.SunEphemerisProvider;
import com.github.mwierzchowski.sun.core.SunEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.LinkedHashMap;

import static java.time.LocalDate.now;

@Component
@RequiredArgsConstructor
public class SunEphemerisInfo implements InfoContributor {
    public static final String ROOT_KEY = "ephemeris";
    public static final String DATE_KEY = "date";

    private final SunEphemerisProvider provider;
    private final Clock clock;

    @Override
    public void contribute(Info.Builder builder) {
        var today = now(clock);
        var details = new LinkedHashMap<String, Object>();
        details.put(DATE_KEY, today);
        provider.sunEphemerisFor(today).stream()
                .forEach(event -> details.put(keyOf(event.getType()), event.getLocalDateTime(clock).toLocalTime()));
        builder.withDetail(ROOT_KEY, details);
    }

    public static String keyOf(SunEventType type) {
        return type.toString().toLowerCase();
    }
}
