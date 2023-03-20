package com.kakao.task

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.extensions.spring.SpringExtension
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["app.scheduling.enable=false"])
class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)
    override fun isolationMode() = IsolationMode.InstancePerLeaf
}

val jsonMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(kotlinModule())