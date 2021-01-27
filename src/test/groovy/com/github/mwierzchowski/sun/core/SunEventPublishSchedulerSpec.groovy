package com.github.mwierzchowski.sun.core

import com.github.mwierzchowski.sun.Integration
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.TaskScheduler
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate

import static com.github.mwierzchowski.sun.core.SunEventType.*
import static java.time.Clock.fixed
import static java.time.LocalDateTime.of
import static java.time.ZoneId.systemDefault
import static java.time.ZoneOffset.UTC

@Integration(properties = [
        "resilience4j.retry.instances.SunEventPublisher.waitDuration=1s",
        "resilience4j.retry.instances.SunEventPublisher.maxRetryAttempts=2"
])
class SunEventPublishSchedulerSpec extends Specification {
    @Autowired
    SunEventPublishScheduler publishScheduler

    @SpringBean
    SunEphemerisProvider ephemerisProvider = Mock()

    @SpringBean
    TaskScheduler taskScheduler = Mock()

    @SpringBean
    Clock clock = fixed(of(2021, 1, 27, 11, 59).toInstant(UTC), systemDefault())

    def "Should plan remaining events for today"() {
        given:
        def today = LocalDate.now(clock)
        ephemerisProvider.sunEphemerisFor(today) >> sunEphemeris(today)
        when:
        publishScheduler.scheduleEvents()
        then:
        3 * taskScheduler.schedule(_, _)
    }

    def "Should retry planning when provider fails"() {
        given:
        def today = LocalDate.now(clock)
        ephemerisProvider.sunEphemerisFor(today) >>> [null, sunEphemeris(today)]
        when:
        publishScheduler.scheduleEvents()
        then:
        3 * taskScheduler.schedule(_, _)
    }

    def "Should plan yesterday events when provider fails providing today ephemeris"() {
        given:
        def today = LocalDate.now(clock)
        def yesterday = today.minusDays(1)
        ephemerisProvider.sunEphemerisFor(today) >> null
        ephemerisProvider.sunEphemerisFor(yesterday) >> sunEphemeris(yesterday, -2)
        when:
        publishScheduler.scheduleEvents()
        then:
        2 * taskScheduler.schedule(_, _)
    }

    def "Should throw exception when provider fails providing both today and yesterday ephemeris"() {
        given:
        def today = LocalDate.now(clock)
        ephemerisProvider.sunEphemerisFor(today) >> null
        ephemerisProvider.sunEphemerisFor(today.minusDays(1)) >> [null]
        when:
        publishScheduler.scheduleEvents()
        then:
        thrown RuntimeException
    }

    def sunEphemeris(LocalDate day, long diff = 0) {
        return new SunEphemeris().tap {
            add(DAWN, day.atTime(6, 0).plusMinutes(diff).atOffset(UTC))
            add(SUNRISE, day.atTime(7, 0).plusMinutes(diff).atOffset(UTC))
            add(NOON, day.atTime(12, 0).plusMinutes(diff).atOffset(UTC))
            add(SUNSET, day.atTime(17, 0).plusMinutes(diff).atOffset(UTC))
            add(DUSK, day.atTime(18, 0).plusMinutes(diff).atOffset(UTC))
        }
    }
}
