package com.github.mwierzchowski.sun.core

import com.github.mwierzchowski.sun.Integration
import org.spockframework.spring.SpringSpy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import spock.lang.Specification

import javax.inject.Provider

import static com.github.mwierzchowski.sun.core.SunEventPublishTask.LOCK_NAME
import static com.github.mwierzchowski.sun.core.SunEventPublishTask.QUEUE_NAME
import static com.github.mwierzchowski.sun.core.SunEventType.NOON
import static java.time.Instant.now

@Integration(properties = ["publish-lock-duration=1s"])
class SunEventPublishTaskSpec extends Specification {
    @Autowired
    Provider<SunEventPublishTask> taskProvider

    @SpringSpy
    RedisTemplate<String, Object> redis

    SunEvent event
    SunEventPublishTask task

    def setup() {
        event = new SunEvent(NOON, now())
        task = taskProvider.get()
        task.setEvent(event)
        redis.delete("job-lock:default:" + LOCK_NAME)
    }

    def "Should publish event"() {
        given:
        def thread = new Thread(task)
        when:
        thread.start()
        thread.join()
        then:
        1 * redis.convertAndSend(QUEUE_NAME, event)
    }

    def "Should not publish event when not a leader"() {
        given:
        def threads = []
        5.times {
            threads << new Thread(task)
        }
        when:
        for (thread in threads) thread.start()
        for (thread in threads) thread.join()
        then:
        1 * redis.convertAndSend(QUEUE_NAME, event)
    }

    def "Should publish event after lock"() {
        given:
        def thread
        when:
        thread = new Thread(task)
        thread.start()
        thread.join()
        Thread.sleep(2000)
        thread = new Thread(task)
        thread.start()
        thread.join()
        then:
        2 * redis.convertAndSend(QUEUE_NAME, event)
    }
}
