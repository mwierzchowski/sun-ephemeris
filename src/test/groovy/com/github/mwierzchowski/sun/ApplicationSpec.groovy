package com.github.mwierzchowski.sun

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@Integration
class ApplicationSpec extends Specification {
    @Autowired
    ApplicationContext context

    def "Should start"() {
        expect:
        context != null
    }
}
