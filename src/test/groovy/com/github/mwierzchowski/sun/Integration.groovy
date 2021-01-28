package com.github.mwierzchowski.sun

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles

import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.TYPE
import static java.lang.annotation.RetentionPolicy.RUNTIME
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK

@Inherited
@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest
@ActiveProfiles
@AutoConfigureWireMock(port = 0)
@interface Integration {
    @AliasFor(annotation = SpringBootTest, attribute = "properties") String[] properties() default []
    @AliasFor(annotation = SpringBootTest, attribute = "webEnvironment") WebEnvironment webEnvironment() default MOCK
    @AliasFor(annotation = ActiveProfiles, attribute = "profiles") String[] profiles() default ["test"]
}