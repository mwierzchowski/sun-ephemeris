package com.github.mwierzchowski.sun.core

import spock.lang.Specification

import static com.github.mwierzchowski.sun.core.SunEventType.NOON

class SunEventTypeSpec extends Specification {
    def "Should provide description as lowercase type name"() {
        given:
        def type = NOON
        expect:
        type.description() == "noon"
    }
}
