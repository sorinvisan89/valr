package com.valr.assignment.model.order

import java.time.Instant

data class OrderBook(
    val asks: MutableSet<Order> = mutableSetOf(),
    val bids: MutableSet<Order> = mutableSetOf(),
    var lastChange: Instant = Instant.now(),
    var sequenceNumber: Long = 0
)
