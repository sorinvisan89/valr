package com.valr.assignment.service

import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.OrderBook
import com.valr.assignment.model.order.Order
import com.valr.assignment.model.order.Side
import com.valr.assignment.model.trade.Trade
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class OrderBookManager(private val idGenerator: IdGenerator) {

    private val log = LoggerFactory.getLogger(OrderBookManager::class.java)

    internal val orderBooks = mutableMapOf<Currency, OrderBook>()
    private val recentTrades = mutableMapOf<Currency, MutableList<Trade>>()

    fun getOrderBook(pair: Currency): OrderBook = orderBooks.getOrDefault(pair, createOrderBook())

    fun getRecentTrades(pair: Currency): List<Trade> =
        recentTrades.getOrDefault(pair, mutableListOf())

    fun placeLimitOrder(order: Order): Order {
        val orderBook = orderBooks.getOrPut(order.pair) { createOrderBook() }
        orderBook.sequenceNumber++

        when (order.side) {
            Side.BUY -> {
                val remainingQuantity = matchOrder(order, orderBook.asks, orderBook, order.pair)
                if (remainingQuantity > 0.0) {
                    // Add remaining quantity as a new buy order
                    val newOrder =
                        order.copy(quantity = remainingQuantity, id = idGenerator.generate())
                    orderBook.bids.add(newOrder)
                }
            }
            Side.SELL -> {
                val remainingQuantity = matchOrder(order, orderBook.bids, orderBook, order.pair)
                if (remainingQuantity > 0.0) {
                    // Add remaining quantity as a new sell order
                    orderBook.asks.add(order.copy(quantity = remainingQuantity, id = idGenerator.generate()))
                }
            }
        }

        orderBook.lastChange = Instant.now()
        return order
    }


    private fun matchOrder(
        order: Order,
        oppositeOrders: MutableSet<Order>,
        orderBook: OrderBook,
        pair: Currency
    ): Double {
        val trades = recentTrades.getOrPut(pair) { mutableListOf() }

        var remainingQuantity = order.quantity

        val ordersToRemove = oppositeOrders.asSequence()
            .filter { oppositeOrder ->
                (order.side == Side.BUY && order.price >= oppositeOrder.price) ||
                        (order.side == Side.SELL && order.price <= oppositeOrder.price)
            }
            .mapNotNull { oppositeOrder ->
                val tradedQuantity = minOf(remainingQuantity, oppositeOrder.quantity)
                val trade = Trade(
                    price = oppositeOrder.price,
                    quantity = tradedQuantity,
                    currencyPair = order.pair,
                    tradedAt = Instant.now(),
                    takerSide = order.side,
                    sequenceId = orderBook.sequenceNumber,
                    id = UUID.randomUUID().toString(),
                    quoteVolume = tradedQuantity * oppositeOrder.price
                )
                trades.add(trade)
                remainingQuantity -= tradedQuantity
                // Update the remaining quantity of the opposite order
                oppositeOrder.quantity -= tradedQuantity

                if (oppositeOrder.quantity <= 0.0) {
                    oppositeOrder // Collect orders to be removed
                } else {
                    null
                }
            }
            .toList()

        if (ordersToRemove.isNotEmpty()) {
            oppositeOrders.removeAll(ordersToRemove)
            log.info("Removed orders ${ordersToRemove.size}")
        }

        return remainingQuantity
    }

    private fun createOrderBook(): OrderBook = OrderBook(
        asks = TreeSet(compareBy<Order> { it.price }
            .thenBy { it.timestamp }
            .thenBy { it.id }
        ),
        bids = TreeSet(compareByDescending<Order> { it.price }
            .thenBy { it.timestamp }
            .thenBy { it.id }
        )
    )

}