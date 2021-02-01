package com.github.mwierzchowski.sun.management

import com.github.mwierzchowski.sun.core.SunEphemeris
import com.github.mwierzchowski.sun.core.SunEphemerisProvider
import org.springframework.boot.actuate.info.Info
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime

import static com.github.mwierzchowski.sun.core.SunEventType.*
import static com.github.mwierzchowski.sun.management.SunEphemerisInfo.*
import static java.time.ZoneId.systemDefault
import static java.time.ZoneOffset.UTC

class SunEphemerisInfoSpec extends Specification {
    def today = LocalDate.of(2021, 1, 31)
    def clock = Clock.fixed(today.atTime(0, 0).toInstant(UTC), systemDefault())
    def provider = Mock(SunEphemerisProvider)
    def info = new SunEphemerisInfo(provider, clock)

    def "Should build ephemeris info"() {
        given:
        provider.sunEphemerisFor(today) >> new SunEphemeris().tap {
            add(DAWN, OffsetDateTime.now(clock))
            add(SUNRISE, OffsetDateTime.now(clock))
            add(NOON, OffsetDateTime.now(clock))
            add(SUNSET, OffsetDateTime.now(clock))
            add(DUSK, OffsetDateTime.now(clock))
        }
        def builder = new Info.Builder()
        when:
        info.contribute(builder)
        def details = builder.build().getDetails().get(ROOT_KEY) as Map<String, Object>
        then:
        details.get(DATE_KEY) != null
        details.get(keyOf(DAWN)) != null
        details.get(keyOf(SUNRISE)) != null
        details.get(keyOf(NOON)) != null
        details.get(keyOf(SUNSET)) != null
        details.get(keyOf(DUSK)) != null
    }
}
