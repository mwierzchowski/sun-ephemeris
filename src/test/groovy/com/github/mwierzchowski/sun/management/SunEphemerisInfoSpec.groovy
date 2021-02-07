package com.github.mwierzchowski.sun.management

import com.github.mwierzchowski.sun.core.SunEphemeris
import com.github.mwierzchowski.sun.core.SunEventPublishScheduler
import org.springframework.boot.actuate.info.Info
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime

import static com.github.mwierzchowski.sun.core.SunEventType.*
import static com.github.mwierzchowski.sun.management.SunEphemerisInfo.NOT_AVAILABLE
import static com.github.mwierzchowski.sun.management.SunEphemerisInfo.ROOT_KEY
import static java.time.ZoneId.systemDefault
import static java.time.ZoneOffset.UTC

class SunEphemerisInfoSpec extends Specification {
    def today = LocalDate.of(2021, 1, 31)
    def clock = Clock.fixed(today.atTime(0, 0).toInstant(UTC), systemDefault())
    def info = new SunEphemerisInfo(clock)

    def "Should provide ephemeris info after success"() {
        given:
        info.onSuccess(new SunEventPublishScheduler.SuccessEvent(ephemeris()))
        def builder = new Info.Builder()
        when:
        info.contribute(builder)
        then:
        def details = builder.build().getDetails().get(ROOT_KEY) as Map<String, Object>
        details.get(DAWN.description()) != null
        details.get(SUNRISE.description()) != null
        details.get(NOON.description()) != null
        details.get(SUNSET.description()) != null
        details.get(DUSK.description()) != null
    }

    def "Should not provide ephemeris info after failure"() {
        given:
        info.onSuccess(new SunEventPublishScheduler.SuccessEvent(ephemeris()))
        info.onFailure(new SunEventPublishScheduler.FailureEvent(new RuntimeException()))
        def builder = new Info.Builder()
        when:
        info.contribute(builder)
        then:
        builder.build().getDetails().get(ROOT_KEY) as String == NOT_AVAILABLE
    }

    def "Should not provide ephemeris when not available yet"() {
        given:
        def builder = new Info.Builder()
        when:
        info.contribute(builder)
        then:
        builder.build().getDetails().get(ROOT_KEY) as String == NOT_AVAILABLE
    }

    def "Should not provide ephemeris when it is not from today"() {
        given:
        info.onSuccess(new SunEventPublishScheduler.SuccessEvent(ephemeris(-1)))
        def builder = new Info.Builder()
        when:
        info.contribute(builder)
        then:
        builder.build().getDetails().get(ROOT_KEY) as String == NOT_AVAILABLE
    }

    def ephemeris(dateDiff = 0) {
        def day = OffsetDateTime.now(clock).plusDays(dateDiff)
        return new SunEphemeris().tap {
            add(DAWN, day)
            add(SUNRISE, day)
            add(NOON, day)
            add(SUNSET, day)
            add(DUSK, day)
        }
    }
}
