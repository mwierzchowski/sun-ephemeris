package com.github.mwierzchowski.sun.management

import com.github.mwierzchowski.sun.core.SunEphemerisProvider
import org.sunrisesunset.model.SunriseSunsetResponse
import spock.lang.Specification

import java.time.format.DateTimeFormatter

import static com.github.mwierzchowski.sun.management.SunEphemerisProviderHealth.*
import static java.time.ZoneId.systemDefault
import static org.springframework.boot.actuate.health.Status.*

class SunEphemerisProviderHealthSpec extends Specification {
    def providerHealth = new SunEphemerisProviderHealth(DateTimeFormatter.ISO_DATE_TIME.withZone(systemDefault()))

    def "Should be UP if last event was success"() {
        given:
        def error = "some critical error"
        providerHealth.onFailure(failureEvent(error))
        providerHealth.onSuccess(successEvent())
        when:
        def health = providerHealth.health()
        then:
        health.status == UP
        health.details[LAST_SUCCESS_TIME_KEY] != NOT_AVAILABLE
        health.details[LAST_FAILURE_TIME_KEY] != NOT_AVAILABLE
        health.details[LAST_FAILURE_MSG_KEY] == error
        health.details[SUCCESS_COUNT_KEY] == 1
        health.details[FAILURE_COUNT_KEY] == 1
    }

    def "Should be UP if there were no failures"() {
        given:
        providerHealth.onSuccess(successEvent())
        when:
        def health = providerHealth.health()
        then:
        health.status == UP
        health.details[LAST_SUCCESS_TIME_KEY] != NOT_AVAILABLE
        health.details[LAST_FAILURE_TIME_KEY] == NOT_AVAILABLE
        health.details[LAST_FAILURE_MSG_KEY] == NOT_AVAILABLE
        health.details[SUCCESS_COUNT_KEY] == 1
        health.details[FAILURE_COUNT_KEY] == 0
    }

    def "Should be DOWN if last event was failure"() {
        given:
        def error = "some critical error"
        providerHealth.onSuccess(successEvent())
        providerHealth.onFailure(failureEvent(error))
        when:
        def health = providerHealth.health()
        then:
        health.status == DOWN
        health.details[LAST_SUCCESS_TIME_KEY] != NOT_AVAILABLE
        health.details[LAST_FAILURE_TIME_KEY] != NOT_AVAILABLE
        health.details[LAST_FAILURE_MSG_KEY] == error
        health.details[SUCCESS_COUNT_KEY] == 1
        health.details[FAILURE_COUNT_KEY] == 1
    }

    def "Should be DOWN if there were no successes"() {
        given:
        providerHealth.onFailure(failureEvent())
        when:
        def health = providerHealth.health()
        then:
        health.status == DOWN
        health.details[LAST_SUCCESS_TIME_KEY] == NOT_AVAILABLE
        health.details[LAST_FAILURE_TIME_KEY] != NOT_AVAILABLE
        health.details[LAST_FAILURE_MSG_KEY] != NOT_AVAILABLE
        health.details[SUCCESS_COUNT_KEY] == 0
        health.details[FAILURE_COUNT_KEY] == 1
    }

    def "Should be UNKNOWN if there were not events"() {
        when:
        def health = providerHealth.health()
        then:
        health.status == UNKNOWN
        health.details[LAST_SUCCESS_TIME_KEY] == NOT_AVAILABLE
        health.details[LAST_FAILURE_TIME_KEY] == NOT_AVAILABLE
        health.details[LAST_FAILURE_MSG_KEY] == NOT_AVAILABLE
        health.details[SUCCESS_COUNT_KEY] == 0
        health.details[FAILURE_COUNT_KEY] == 0
    }

    def failureEvent(message = "test error", sleep = 10) {
        def event = new SunEphemerisProvider.FailureEvent(new RuntimeException(message))
        Thread.sleep(sleep)
        return event
    }

    def successEvent(sleep = 10) {
        def event = new SunEphemerisProvider.SuccessEvent(new SunriseSunsetResponse())
        Thread.sleep(sleep)
        return event
    }
}
