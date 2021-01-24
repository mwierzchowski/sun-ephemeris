package com.github.mwierzchowski.sun

import com.github.mwierzchowski.sun.core.SunEvent
import spock.lang.Specification

import java.time.Clock

import static java.time.Instant.now
import static java.time.ZoneId.systemDefault

class SunEventSpec extends Specification {
    def nowTimestamp = now()
    def clock = Clock.fixed(nowTimestamp, systemDefault())
    def event = new SunEvent()

    def "Should be stale when timestamp is in the past"() {
        given:
        event.setTimestamp(nowTimestamp.minusSeconds(1))
        expect:
        event.isStale(clock)
    }

    def "Should not be stale when timestamp is in the future"() {
        given:
        event.setTimestamp(nowTimestamp.plusSeconds(1))
        expect:
        !event.isStale(clock)
    }

    def "Should provide local date time"() {
        given:
        event.setTimestamp(nowTimestamp)
        expect:
        event.getLocalDateTime(clock) != null
    }
}
