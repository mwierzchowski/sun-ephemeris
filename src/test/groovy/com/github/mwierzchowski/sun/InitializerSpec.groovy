package com.github.mwierzchowski.sun

import com.github.mwierzchowski.sun.core.SunEventPublishScheduler
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import spock.lang.Specification

@Integration(properties = ["sun-ephemeris.init-on-startup=true"])
class InitializerSpec extends Specification {
    @Autowired
    ApplicationListener<ApplicationReadyEvent> initializer

    @SpringBean
    SunEventPublishScheduler publishScheduler = Mock()

    def "Should schedule events"() {
        expect:
        initializer != null
    }
}
