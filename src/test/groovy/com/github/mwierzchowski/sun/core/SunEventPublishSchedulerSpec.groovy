package com.github.mwierzchowski.sun.core

import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.TaskScheduler
import spock.lang.Specification

import javax.inject.Provider
import java.time.Clock
import java.time.LocalDate

import static com.github.mwierzchowski.sun.core.SunEventType.*
import static java.time.Clock.fixed
import static java.time.LocalDateTime.of
import static java.time.ZoneId.systemDefault
import static java.time.ZoneOffset.UTC

class SunEventPublishSchedulerSpec extends Specification {
    SunEphemerisProvider ephemerisProvider = Mock()
    Provider<SunEventPublishTask> taskProvider = Mock() {
        get() >> new SunEventPublishTask(null)
    }
    TaskScheduler taskScheduler = Mock()
    ApplicationEventPublisher statusPublisher = Mock()
    Clock clock = fixed(of(2021, 1, 27, 11, 59).toInstant(UTC), systemDefault())
    SunEventPublishScheduler publishScheduler = new SunEventPublishScheduler(ephemerisProvider, taskProvider, taskScheduler, statusPublisher, clock)

    def "Should plan remaining events for today"() {
        given:
        def today = LocalDate.now(clock)
        ephemerisProvider.sunEphemerisFor(today) >> sunEphemeris(today)
        when:
        publishScheduler.scheduleEvents()
        then:
        3 * taskScheduler.schedule(_, _)
        1 * statusPublisher.publishEvent(_ as SunEventPublishScheduler.SuccessEvent)
    }

    def "Should fallback to yesterday events in case of issues"() {
        given:
        def exception = new RuntimeException("testing fallback")
        def yesterday = LocalDate.now(clock).minusDays(1)
        ephemerisProvider.sunEphemerisFor(yesterday) >> sunEphemeris(yesterday, -2)
        when:
        publishScheduler.scheduleEventsFallback(exception)
        then:
        2 * taskScheduler.schedule(_, _)
        1 * statusPublisher.publishEvent(_ as SunEventPublishScheduler.FailureEvent)
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
