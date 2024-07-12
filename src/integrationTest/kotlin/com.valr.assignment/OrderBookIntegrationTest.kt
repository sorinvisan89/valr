package com.valr.assignment.integration

import com.valr.assignment.dto.OrderDTO
import com.valr.assignment.dto.OrderRequestDTO
import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.OrderBook
import com.valr.assignment.model.order.Side
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import java.util.*

@ExtendWith(SpringExtension::class)
class OrderBookIntegrationTest {

    companion object {

        private val versionProps by lazy {
            Properties().also {
                it.load(this::class.java.getResourceAsStream("/version.properties"))
            }
        }

        private val buildVersion by lazy {
            versionProps.getProperty("version") ?: "latest"
        }

        val appContainer = GenericContainer<Nothing>("com.valr/assignment:${buildVersion}").apply {
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

    @Test
    fun `test place limit order`() {

        val restTemplate = TestRestTemplate()
        val port = appContainer.getMappedPort(8081)

        val submitUrl = "http://localhost:${port}/api/orderbook/orders"

        val request = OrderRequestDTO(
            price = 50000,
            quantity = 1.0,
            pair = Currency.BTCZAR,
            side = Side.SELL
        )

        val response = restTemplate.postForEntity(submitUrl, request, OrderDTO::class.java)
        response.statusCode.value() shouldBe HttpStatus.OK.value()
        response.body shouldNotBe null
        val body = response.body!!
        body.id shouldNotBe null
        body.pair shouldBe Currency.BTCZAR
        body.quantity shouldBe 1.0
        body.price shouldBe 50000

        val retrieveUrl = "http://localhost:${port}/api/orderbook/BTCZAR"

        val retrieveResponse = restTemplate.getForEntity(retrieveUrl, OrderBook::class.java)
        retrieveResponse.statusCode.value() shouldBe HttpStatus.OK.value()
        retrieveResponse.body shouldNotBe null
        val orders = retrieveResponse.body!!
        orders.asks.size shouldBe 1
        orders.asks.first().id shouldNotBe null
        orders.asks.first().quantity shouldBe 1.0
        orders.asks.first().pair shouldBe Currency.BTCZAR
        orders.asks.first().price shouldBe 50000
        orders.asks.first().side shouldBe Side.SELL
        orders.asks.first().timestamp shouldNotBe null
    }
}