package com.github.mwierzchowski.sun.api.v1

import com.github.mwierzchowski.sun.Integration
import com.github.mwierzchowski.sun.core.SunEphemeris
import com.github.mwierzchowski.sun.core.SunEphemerisProvider
import com.github.mwierzchowski.sun.core.SunEvent
import com.github.mwierzchowski.sun.core.SunEventType
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate

import static com.github.mwierzchowski.sun.core.SunEventType.*
import static java.time.Clock.fixed
import static java.time.LocalDateTime.of
import static java.time.ZoneId.systemDefault
import static java.time.ZoneOffset.UTC
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Integration(webEnvironment = RANDOM_PORT)
class SunEventsControllerV1Spec extends Specification {
    @SpringBean
    SunEphemerisProvider ephemerisProvider = Mock()

    @SpringBean
    Clock clock = fixed(of(2021, 1, 27, 11, 59).toInstant(UTC), systemDefault())

    @Autowired
    TestRestTemplate restTemplate

    def "Should provide events for today when date is not provided"() {
        given:
        def today = LocalDate.now(clock)
        ephemerisProvider.sunEphemerisFor(today) >> sunEphemeris(today)
        when:
        def response = restTemplate.getForEntity("/v1/events", SunEvent[])
        then:
        response.statusCodeValue == 200
        response.body.size() == SunEventType.values().size()
    }

    def "Should provide events for given date"() {
        given:
        def requestDate = LocalDate.now(clock).plusDays(1)
        ephemerisProvider.sunEphemerisFor(requestDate) >> sunEphemeris(requestDate)
        when:
        def response = restTemplate.getForEntity("/v1/events?date=${requestDate}", SunEvent[])
        then:
        response.statusCodeValue == 200
        response.body.size() == SunEventType.values().size()
    }

    def "Should provide next today's event after now"() {
        given:
        def today = LocalDate.now(clock)
        ephemerisProvider.sunEphemerisFor(today) >> sunEphemeris(today)
        when:
        def response = restTemplate.getForEntity("/v1/events/next", SunEvent)
        then:
        response.statusCodeValue == 200
        response.body.type == NOON
    }

    def "Should provide first tomorrow's event if today is too late"() {
        given:
        def today = LocalDate.now(clock)
        ephemerisProvider.sunEphemerisFor(today) >> sunEphemeris(today, 6 * 60)
        ephemerisProvider.sunEphemerisFor(today.plusDays(1)) >> sunEphemeris(today)
        when:
        def response = restTemplate.getForEntity("/v1/events/next", SunEvent)
        then:
        response.statusCodeValue == 200
        response.body.type == DAWN
    }

    def sunEphemeris(LocalDate day, long diff = 0) {
        return new SunEphemeris().tap {
            add(DAWN, day.atTime(7, 0).minusMinutes(diff).atOffset(UTC))
            add(SUNRISE, day.atTime(8, 0).minusMinutes(diff).atOffset(UTC))
            add(NOON, day.atTime(12, 0).minusMinutes(diff).atOffset(UTC))
            add(SUNSET, day.atTime(16, 0).minusMinutes(diff).atOffset(UTC))
            add(DUSK, day.atTime(17, 0).minusMinutes(diff).atOffset(UTC))
        }
    }
}
