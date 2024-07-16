package com.valr.assignment.integration

import com.valr.assignment.AbstractContainerSetup
import com.valr.assignment.dto.AuthenticationRequestDTO
import com.valr.assignment.dto.AuthenticationResponseDTO
import com.valr.assignment.dto.OrderDTO
import com.valr.assignment.dto.OrderRequestDTO
import com.valr.assignment.dto.TradeDTO
import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.OrderBook
import com.valr.assignment.model.order.Side
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class OrderBookIntegrationTest : AbstractContainerSetup() {

    @Test
    fun `test place limit order`() {
        val port = appContainer.getMappedPort(8081)
        val submitUrl = "http://localhost:$port/api/orderbook/orders"

        val request = OrderRequestDTO(
            price = 50000,
            quantity = 1.0,
            pair = Currency.BTCZAR,
            side = Side.SELL
        )

        val headers = generateAuthHeaders()
        val response = restTemplate.exchange(
            submitUrl,
            HttpMethod.POST,
            HttpEntity(request, headers),
            OrderDTO::class.java
        )
        response.statusCode.value() shouldBe HttpStatus.OK.value()
        response.body shouldNotBe null
        val body = response.body!!
        body.id shouldNotBe null
        body.pair shouldBe Currency.BTCZAR
        body.quantity shouldBe 1.0
        body.price shouldBe 50000

        val retrieveUrl = "http://localhost:$port/api/orderbook/BTCZAR"

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

    @Test
    fun `test place limit order ascending and then matches all and clears`() {
        val port = appContainer.getMappedPort(8081)
        val submitUrl = "http://localhost:$port/api/orderbook/orders"

        val currency = Currency.BTCZAR

        val headers = generateAuthHeaders()

        for (i in 1..10) {
            val sellRequest = OrderRequestDTO(
                price = 1000L * i,
                quantity = 1.0,
                pair = currency,
                side = Side.SELL
            )

            val response = restTemplate.exchange(
                submitUrl,
                HttpMethod.POST,
                HttpEntity(sellRequest, headers),
                OrderDTO::class.java
            )

            response.statusCode.value() shouldBe HttpStatus.OK.value()
        }

        assertTradeCount(currency, 0)

        for (i in 1..10) {
            val buyRequest = OrderRequestDTO(
                price = 1000L * i,
                quantity = 1.0,
                pair = currency,
                side = Side.BUY
            )

            val response = restTemplate.exchange(
                submitUrl,
                HttpMethod.POST,
                HttpEntity(buyRequest, headers),
                OrderDTO::class.java
            )

            response.statusCode.value() shouldBe HttpStatus.OK.value()
        }

        assertTradeCount(currency, 10)
    }

    private fun generateAuthHeaders(): HttpHeaders {
        val authRequest = AuthenticationRequestDTO(email = "first@gmail.com", password = "pass1")
        val authResponse = restTemplate.postForEntity(
            "http://localhost:${appContainer.getMappedPort(8081)}/api/auth",
            HttpEntity(authRequest, HttpHeaders()),
            AuthenticationResponseDTO::class.java
        )

        authResponse.statusCode.value() shouldBe HttpStatus.OK.value()
        authResponse.body shouldNotBe null
        val token = authResponse.body!!.accessToken

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $token")
        return headers
    }

    private fun assertTradeCount(currency: Currency, expectedTrades: Int) {
        val retrieveTradesUrl =
            "http://localhost:${appContainer.getMappedPort(8081)}/api/orderbook/trades/$currency"

        val responseType = object : ParameterizedTypeReference<List<TradeDTO>>() {}

        val tradesResponse = restTemplate.exchange(
            retrieveTradesUrl,
            HttpMethod.GET,
            null,
            responseType
        )
        tradesResponse.statusCode.value() shouldBe HttpStatus.OK.value()
        tradesResponse.body shouldNotBe null
        tradesResponse.body!!.size shouldBe expectedTrades
    }
}
