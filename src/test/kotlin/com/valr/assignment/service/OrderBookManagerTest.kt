package com.valr.assignment.service

import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.Order
import com.valr.assignment.model.order.OrderBook
import com.valr.assignment.model.order.Side
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import java.time.Instant


class OrderBookManagerTest : FunSpec({

    fun createOrderBookManager() = OrderBookManager()

    test("getOrderBook returns a new OrderBook if not present") {
        val orderBookManager = createOrderBookManager()
        val currency = Currency.BTCZAR
        val orderBook = orderBookManager.getOrderBook(currency)
        orderBook.asks shouldHaveSize 0
        orderBook.bids shouldHaveSize 0
    }

    test("placeLimitOrder adds a new buy order") {
        val orderBookManager = createOrderBookManager()
        val currency = Currency.BTCZAR
        val order = Order(
            id = "1",
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.BUY,
            timestamp = Instant.now()
        )

        orderBookManager.placeLimitOrder(order)

        val orderBook = orderBookManager.getOrderBook(currency)
        orderBook.bids shouldHaveSize 1
        orderBook.asks shouldHaveSize 0
    }

    test("placeLimitOrder adds a new sell order") {
        val orderBookManager = createOrderBookManager()
        val currency = Currency.BTCZAR
        val order = Order(
            id = "1",
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.SELL,
            timestamp = Instant.now()
        )

        orderBookManager.placeLimitOrder(order)

        val orderBook = orderBookManager.getOrderBook(currency)
        orderBook.bids shouldHaveSize 0
        orderBook.asks shouldHaveSize 1
    }

    test("matchOrder matches and removes orders correctly") {
        val orderBookManager = createOrderBookManager()
        val currency = Currency.BTCZAR

        // Create and place a sell order
        val sellOrder = Order(
            id = "1",
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.SELL,
            timestamp = Instant.now()
        )
        orderBookManager.placeLimitOrder(sellOrder)

        // Create and place a buy order that matches the sell order
        val buyOrder = Order(
            id = "2",
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.BUY,
            timestamp = Instant.now()
        )
        orderBookManager.placeLimitOrder(buyOrder)

        val orderBook = orderBookManager.getOrderBook(currency)
        orderBook.bids shouldHaveSize 0
        orderBook.asks shouldHaveSize 0

        val trades = orderBookManager.getRecentTrades(currency)
        trades shouldHaveSize 1
        trades[0].price shouldBeExactly 50000
        trades[0].quantity shouldBeExactly 1.0
    }

    test("matchOrder partial match") {
        val orderBookManager = createOrderBookManager()
        val currency = Currency.BTCZAR

        // Create and place a sell order
        val sellOrder = Order(
            id = "1",
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.SELL,
            timestamp = Instant.now()
        )
        orderBookManager.placeLimitOrder(sellOrder)

        // Create and place a buy order that partially matches the sell order
        val buyOrder = Order(
            id = "2",
            price = 50000,
            quantity = 0.5,
            pair = currency,
            side = Side.BUY,
            timestamp = Instant.now()
        )
        orderBookManager.placeLimitOrder(buyOrder)

        val orderBook = orderBookManager.getOrderBook(currency)
        orderBook.bids shouldHaveSize 0
        orderBook.asks shouldHaveSize 1
        orderBook.asks.first().quantity shouldBeExactly 0.5

        val trades = orderBookManager.getRecentTrades(currency)
        trades shouldHaveSize 1
        trades[0].price shouldBeExactly 50000
        trades[0].quantity shouldBeExactly 0.5
    }

    test("Order matching overfills demand") {
        val orderBookManager = createOrderBookManager()

        withClue("Assume we have an existing order book with some orders") {
            val pair = Currency.BTCZAR
            val initialOrderBook = OrderBook(
                asks = mutableSetOf(
                    Order(id = "1", side = Side.SELL, quantity = 0.5, price = 1000000, pair = pair),
                    Order(id = "2", side = Side.SELL, quantity = 1.0, price = 1000000, pair = pair)
                ),
                lastChange = Instant.now(),
                sequenceNumber = 1
            )

            orderBookManager.orderBooks[pair] = initialOrderBook

            // Place an order that overfills the demand
            val overfillOrder =
                Order(id = "4", side = Side.BUY, quantity = 2.0, price = 1000000, pair = pair)
            val resultOrder = orderBookManager.placeLimitOrder(overfillOrder)

            withClue("Assert that the result order has been partially filled") {
                resultOrder.quantity shouldBe 0.5
            }

            withClue("Assert that the order book state has been updated") {
                val updatedOrderBook = orderBookManager.getOrderBook(pair)
                updatedOrderBook.asks.size shouldBe 0
                updatedOrderBook.bids.size shouldBe 1
                val generatedOrder = updatedOrderBook.bids.first()
                withClue("The overfilling ammount was place as a new order") {
                    generatedOrder shouldBe Order(
                        id = "4",
                        side = Side.BUY,
                        quantity = 0.5,
                        price = 1000000,
                        pair = pair,
                        timestamp = generatedOrder.timestamp
                    )
                }
            }
        }
    }
})