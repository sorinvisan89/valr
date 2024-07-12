package com.valr.assignment.dto

import com.valr.assignment.model.order.OrderBook
import java.time.Instant

data class OrderBookDTO(
    val asks: Set<OrderDTO>,
    val bids: Set<OrderDTO>,
    var lastChange: Instant,
    var sequenceNumber: Long
) {

    companion object {
        fun fromOrderBook(orderBook: OrderBook) = OrderBookDTO(
            asks = orderBook.asks.map { OrderDTO.fromOrder(it) }.toSet(),
            bids = orderBook.bids.map { OrderDTO.fromOrder(it) }.toSet(),
            lastChange = orderBook.lastChange,
            sequenceNumber = orderBook.sequenceNumber,
        )
    }
}