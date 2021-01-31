package com.github.mwierzchowski.sun

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles

import java.lang.annotation.Retention

import static java.lang.annotation.RetentionPolicy.RUNTIME
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK

@Retention(RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@interface Integration {
    @AliasFor(annotation = SpringBootTest, attribute = "properties") String[] properties() default []
    @AliasFor(annotation = SpringBootTest, attribute = "webEnvironment") WebEnvironment webEnvironment() default MOCK
}