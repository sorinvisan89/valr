package com.valr.assignment

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.web.client.TestRestTemplate
import org.testcontainers.containers.GenericContainer
import java.util.Properties

abstract class AbstractContainerSetup {

    companion object {

        private val versionProps by lazy {
            Properties().also {
                it.load(this::class.java.getResourceAsStream("/version.properties"))
            }
        }

        private val buildVersion by lazy {
            versionProps.getProperty("version") ?: "latest"
        }

        val restTemplate by lazy {
            TestRestTemplate()
        }

        val appContainer = GenericContainer<Nothing>("com.valr/assignment:$buildVersion").apply {
            withExposedPorts(8081)
            withEnv("SPRING_PROFILES_ACTIVE", "test")
        }

        @BeforeAll
        @JvmStatic
        fun startContainer() {
            appContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun stopContainer() {
            appContainer.stop()
        }
    }
}
