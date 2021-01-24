package com.github.mwierzchowski.sun

import com.github.mwierzchowski.sun.core.SunEphemerisProvider
import com.github.mwierzchowski.sun.core.SunEventType
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate

import static com.github.mwierzchowski.sun.core.SunEphemerisProvider.CACHE_NAME
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import static org.apache.http.HttpHeaders.CONTENT_TYPE
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Integration(properties = ["resilience4j.retry.instances.SunEphemerisProvider.maxRetryAttempts=2"])
class SunEphemerisProviderSpec extends Specification {
    @Autowired
    SunEphemerisProvider provider

    @Autowired
    CacheManager cacheManager

    @Shared
    UrlPattern apiUrl = urlPathMatching("/sunrise-sunset/.*")

    LocalDate today = LocalDate.of(2019, 7, 4)

    def setup() {
        reset()
        cacheManager.getCache(CACHE_NAME).clear()
    }

    def "Should provide ephemeris"() {
        given:
        stubFor(get(apiUrl).willReturn(aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBodyFile("sunrise-sunset.json")))
        when:
        def ephemeris = provider.sunEphemerisFor(today)
        then:
        ephemeris.firstEvent() != null
        ephemeris.stream().count() == SunEventType.values().size()
        verify(1, getRequestedFor(apiUrl))
    }

    def "Should cache today ephemeris"() {
        given:
        stubFor(get(apiUrl).willReturn(aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBodyFile("sunrise-sunset.json")))
        when:
        def ephemeris1 = provider.sunEphemerisFor(today)
        def ephemeris2 = provider.sunEphemerisFor(today)
        then:
        ephemeris1 != null
        ephemeris2 != null
        ephemeris1 == ephemeris2
        verify(1, getRequestedFor(apiUrl))
    }

    def "Should request ephemeris for a new date"() {
        given:
        stubFor(get(apiUrl).willReturn(aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBodyFile("sunrise-sunset.json")))
        when:
        def ephemeris1 = provider.sunEphemerisFor(today)
        def ephemeris2 = provider.sunEphemerisFor(today.plusDays(1))
        then:
        verify(2, getRequestedFor(apiUrl))
    }

    def "Should retry request when it fails"() {
        given:
        stubFor(get(apiUrl).inScenario("Retries")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(SC_SERVICE_UNAVAILABLE))
                .willSetStateTo("Second call"))
        stubFor(get(apiUrl).inScenario("Retries")
                .whenScenarioStateIs("Second call")
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("sunrise-sunset.json")))
        when:
        def ephemeris = provider.sunEphemerisFor(today)
        then:
        ephemeris != null
        verify(2, getRequestedFor(apiUrl))
    }

    def "Should throw exception when sunrise-sunset service is down"() {
        given:
        def errorMessage = "Some critical error"
        stubFor(get(apiUrl).willReturn(aResponse()
                .withStatus(SC_SERVICE_UNAVAILABLE)
                .withBody(errorMessage)))
        when:
        provider.sunEphemerisFor(today)
        then:
        def ex = thrown(RuntimeException)
        ex.getMessage().contains(errorMessage)
        verify(2, getRequestedFor(apiUrl))
    }

    def "Should throw exception when sunrise-sunset service returns empty response"() {
        given:
        stubFor(get(apiUrl).willReturn(aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody("{}")))
        when:
        provider.sunEphemerisFor(today)
        then:
        thrown RuntimeException
    }
}
