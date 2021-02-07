package com.github.mwierzchowski.sun.core

import spock.lang.Specification

import java.time.Clock

import static com.github.mwierzchowski.sun.core.SunEventType.NOON
import static java.time.Instant.now
import static java.time.ZoneId.systemDefault

class SunEventSpec extends Specification {
    def nowTimestamp = now()
    def clock = Clock.fixed(nowTimestamp, systemDefault())
    def event = new SunEvent()

    def "Should be stale when timestamp is in the past"() {
        given:
        event.timestamp = nowTimestamp.minusSeconds(1)
        expect:
        event.isStale(clock)
    }

    def "Should not be stale when timestamp is in the future"() {
        given:
        event.timestamp = nowTimestamp.plusSeconds(1)
        expect:
        !event.isStale(clock)
    }

    def "Should provide local time"() {
        given:
        event.timestamp = nowTimestamp
        expect:
        event.getLocalTime(clock) != null
    }

    def "Should provide event name"() {
        given:
        event.type = NOON
        expect:
        event.getName() == "noon"
    }
}
