package com.github.mwierzchowski.sun.core

import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.stream.Collectors

import static com.github.mwierzchowski.sun.core.SunEventType.*
import static java.time.ZoneId.systemDefault
import static java.time.ZoneOffset.UTC

class SunEphemerisSpec extends Specification {
    def today = LocalDate.of(2021, 01, 24)
    def sunriseTime = OffsetDateTime.of(today, LocalTime.of(5, 30), UTC)
    def noonTime = OffsetDateTime.of(today, LocalTime.NOON, UTC)
    def sunsetTime = OffsetDateTime.of(today, LocalTime.of(16, 15), UTC)
    def ephemeris = new SunEphemeris()

    def "Should stream events in correct order"() {
        given:
        ephemeris.add(SUNSET, sunsetTime)
        ephemeris.add(SUNRISE, sunriseTime)
        ephemeris.add(NOON, noonTime)
        when:
        def events = ephemeris.stream().collect(Collectors.toList())
        then:
        events.size() == 3
        with (events[0]) {
            type == SUNRISE
            timestamp == sunriseTime.toInstant()
        }
        with (events[1]) {
            type == NOON
            timestamp == noonTime.toInstant()
        }
        with (events[2]) {
            type == SUNSET
            timestamp == sunsetTime.toInstant()
        }
    }

    def "Should find first event"() {
        given:
        ephemeris.add(SUNSET, sunsetTime)
        ephemeris.add(SUNRISE, sunriseTime)
        ephemeris.add(NOON, noonTime)
        when:
        def firstEvent = ephemeris.firstEvent()
        then:
        firstEvent.type == SUNRISE
        firstEvent.timestamp == sunriseTime.toInstant()
    }

    def "Should throw exception when there is no first event"() {
        when:
        ephemeris.firstEvent()
        then:
        thrown RuntimeException
    }

    def "Should find first event after now"() {
        given:
        ephemeris.add(SUNRISE, sunriseTime)
        ephemeris.add(NOON, noonTime)
        ephemeris.add(SUNSET, sunsetTime)
        when:
        def optionalFirstEvent = ephemeris.firstEventAfter(noonTime.plusSeconds(1).toInstant())
        then:
        optionalFirstEvent.isPresent()
        optionalFirstEvent.get().type == SUNSET
        optionalFirstEvent.get().timestamp == sunsetTime.toInstant()
    }

    def "Should not find first event when last event passed"() {
        given:
        ephemeris.add(SUNRISE, sunriseTime)
        ephemeris.add(NOON, noonTime)
        ephemeris.add(SUNSET, sunsetTime)
        when:
        def optionalFirstEvent = ephemeris.firstEventAfter(sunsetTime.plusSeconds(1).toInstant())
        then:
        !optionalFirstEvent.isPresent()
    }

    def "Should be marked as today when noon is today"() {
        given:
        ephemeris.add(NOON, noonTime)
        def testClock = Clock.fixed(noonTime.toInstant(), systemDefault())
        expect:
        ephemeris.isToday(testClock)
    }

    def "Should be marked as not today when noon is not today"() {
        given:
        ephemeris.add(NOON, noonTime)
        def testClock = Clock.fixed(noonTime.minusDays(1).toInstant(), systemDefault())
        expect:
        !ephemeris.isToday(testClock)
    }
}
