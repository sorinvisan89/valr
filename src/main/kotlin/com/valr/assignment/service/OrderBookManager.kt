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
class OrderBookManager {

    private val log = LoggerFactory.getLogger(OrderBookManager::class.java)

    internal val orderBooks = mutableMapOf<Currency, OrderBook>()
    private val recentTrades = mutableMapOf<Currency, MutableList<Trade>>()

    fun getOrderBook(pair: Currency): OrderBook = orderBooks.getOrDefault(pair, createOrderBook())

    fun getRecentTrades(pair: Currency): List<Trade> =
        recentTrades.getOrDefault(pair, mutableListOf())

    fun placeLimitOrder(order: Order): Order {
        val orderBook = orderBooks.getOrPut(order.pair) { createOrderBook() }
        orderBook.sequenceNumber.inc()

        when (order.side) {
            Side.BUY -> {
                matchOrder(order, orderBook.asks, orderBook, order.pair)
                if (order.quantity > 0) {
                    orderBook.bids.add(order)  // Add remaining quantity as a new buy order
                }
            }
            Side.SELL -> {
                matchOrder(order, orderBook.bids, orderBook, order.pair)
                if (order.quantity > 0) {
                    orderBook.asks.add(order)  // Add remaining quantity as a new sell order
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
    ) {
        val trades = recentTrades.getOrPut(pair) { mutableListOf() }

        val iterator = oppositeOrders.iterator()
        while (iterator.hasNext()) {
            val oppositeOrder = iterator.next()
            if ((order.side == Side.BUY && order.price >= oppositeOrder.price) ||
                (order.side == Side.SELL && order.price <= oppositeOrder.price)
            ) {
                val tradedQuantity = minOf(order.quantity, oppositeOrder.quantity)
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
                order.quantity -= tradedQuantity
                oppositeOrder.quantity -= tradedQuantity

                if (oppositeOrder.quantity == 0.0) {
                    iterator.remove()  // Remove from oppositeOrders if quantity is zero
                }

                if (order.quantity == 0.0) {
                    break
                }
            }
        }
    }

    private fun createOrderBook(): OrderBook = OrderBook(
        asks = TreeSet(compareBy<Order> { it.price }),
        bids = TreeSet(compareByDescending<Order> { it.price })
    )

}