package com.valr.assignment.service

import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.Order
import com.valr.assignment.model.order.OrderBook
import com.valr.assignment.model.order.Side
import com.valr.assignment.utils.Locker
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.*


class OrderBookManagerTest : FunSpec({

    val idGenerator = mockk<IdGenerator>()
    val locker = object : Locker {
        override fun <T> withReadLock(block: () -> T): T = block()

        override fun <T> withWriteLock(block: () -> T): T = block()
    }
    val firstGeneratedId = UUID.fromString("65e0a581-971b-4520-974b-d0535c91e744").toString()
    val secondGeneratedId = UUID.fromString("8118466d-58da-434a-8072-c3f54fec7e08").toString()

    fun createOrderBookManager() = OrderBookManager(idGenerator, locker)

    beforeAny {
        clearAllMocks()
        every {
            idGenerator.generate()
        } returnsMany listOf(firstGeneratedId, secondGeneratedId)
    }

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
            id = UUID.randomUUID().toString(),
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.BUY,
            timestamp = Instant.now()
        )

        orderBookManager.placeLimitOrder(order)

        val orderBook = orderBookManager.getOrderBook(currency)

        orderBook.asks shouldHaveSize 0
        orderBook.bids shouldHaveSize 1
        orderBook.bids.first() shouldBe order.copy(id = firstGeneratedId)

        orderBookManager.getRecentTrades(currency).size shouldBe 0
    }

    test("placeLimitOrder adds a new sell order") {
        val orderBookManager = createOrderBookManager()
        val currency = Currency.BTCZAR
        val order = Order(
            id = UUID.randomUUID().toString(),
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
        orderBook.asks.first() shouldBe order.copy(id = firstGeneratedId)

        orderBookManager.getRecentTrades(currency).size shouldBe 0
    }

    test("matchOrder matches and removes orders correctly") {
        val orderBookManager = createOrderBookManager()
        val currency = Currency.BTCZAR

        // Create and place a sell order
        val sellOrder = Order(
            id = UUID.randomUUID().toString(),
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.SELL,
            timestamp = Instant.now()
        )
        orderBookManager.placeLimitOrder(sellOrder)

        // Create and place a buy order that matches the sell order
        val buyOrder = Order(
            id = UUID.randomUUID().toString(),
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
            id = UUID.randomUUID().toString(),
            price = 50000,
            quantity = 1.0,
            pair = currency,
            side = Side.SELL,
            timestamp = Instant.now()
        )
        orderBookManager.placeLimitOrder(sellOrder)

        // Create and place a buy order that partially matches the sell order
        val buyOrder = Order(
            id = UUID.randomUUID().toString(),
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
        val generatedOrder = orderBook.asks.first()
        withClue("The overfilling amount was place as a new order") {
            generatedOrder shouldBe Order(
                id = firstGeneratedId,
                side = Side.SELL,
                quantity = 0.5,
                price = 50000,
                pair = currency,
                timestamp = generatedOrder.timestamp
            )
        }

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
                    Order(id = UUID.randomUUID().toString(), side = Side.SELL, quantity = 0.5, price = 1000000, pair = pair),
                    Order(id = UUID.randomUUID().toString(), side = Side.SELL, quantity = 1.0, price = 1100000, pair = pair)
                ),
                lastChange = Instant.now(),
                sequenceNumber = 1
            )

            orderBookManager.orderBooks[pair] = initialOrderBook

            // Place an order that overfills the demand
            val overfillOrder =
                Order(id = UUID.randomUUID().toString(), side = Side.BUY, quantity = 2.0, price = 1200000, pair = pair)
            val resultOrder = orderBookManager.placeLimitOrder(overfillOrder)

            // Assert that the result order has been filled
            resultOrder.quantity shouldBe 2.0

            // Assert that the order book state has been updated
            val updatedOrderBook = orderBookManager.getOrderBook(pair)
            updatedOrderBook.asks.size shouldBe 0
            updatedOrderBook.bids.size shouldBe 1
            val generatedOrder = updatedOrderBook.bids.first()
            withClue("The overfilling amount was place as a new order") {
                generatedOrder shouldBe Order(
                    id = firstGeneratedId,
                    side = Side.BUY,
                    quantity = 0.5,
                    price = 1200000L,
                    pair = pair,
                    timestamp = generatedOrder.timestamp
                )
            }

            // Check the matched trades
            val trades = orderBookManager.getRecentTrades(pair)
            trades shouldHaveSize 2
            trades[0].price shouldBeExactly 1000000
            trades[0].quantity shouldBeExactly 0.5
            trades[1].price shouldBeExactly 1100000
            trades[1].quantity shouldBeExactly 1.0

        }
    }

    test("Order matching doesn't match on last order prices") {
        val orderBookManager = createOrderBookManager()

        withClue("Assume we have an existing order book with some orders") {
            val pair = Currency.BTCZAR
            val initialOrderBook = OrderBook(
                asks = mutableSetOf(
                    Order(id = "1", side = Side.SELL, quantity = 0.5, price = 1000000, pair = pair),
                    Order(id = "2", side = Side.SELL, quantity = 1.0, price = 1100000, pair = pair)
                ),
                lastChange = Instant.now(),
                sequenceNumber = 1
            )

            orderBookManager.orderBooks[pair] = initialOrderBook

            // Place an order that overfills the demand
            val overfillOrder =
                Order(id = "4", side = Side.BUY, quantity = 2.0, price = 1000500, pair = pair)

            val resultOrder = orderBookManager.placeLimitOrder(overfillOrder)
            resultOrder.id shouldNotBe null
            resultOrder.quantity shouldBe 2.0

            // Assert that the order book state has been updated
            val updatedOrderBook = orderBookManager.getOrderBook(pair)
            updatedOrderBook.asks.size shouldBe 2
            updatedOrderBook.bids.size shouldBe 1
            val generatedOrder = updatedOrderBook.bids.first()
            withClue("The overfilling amount was place as a new order") {
                generatedOrder shouldBe Order(
                    id = firstGeneratedId,
                    side = Side.BUY,
                    quantity = 1.5,
                    price = 1000500,
                    pair = pair,
                    timestamp = generatedOrder.timestamp
                )
            }

            // Check the matched trades which contains the first partial match
            val trades = orderBookManager.getRecentTrades(pair)
            trades shouldHaveSize 1
            trades[0].price shouldBeExactly 1000000
            trades[0].quantity shouldBeExactly 0.5
        }
    }
})