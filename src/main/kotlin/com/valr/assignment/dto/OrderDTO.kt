package com.valr.assignment.dto

import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.Order
import com.valr.assignment.model.order.Side
import java.time.Instant

data class OrderDTO(
    val id: String,
    val side: Side,
    val price: Long,
    val quantity: Double,
    val pair: Currency,
    val timestamp: Instant,
) {
    companion object {
        fun fromOrder(order: Order) = OrderDTO(
            id = order.id,
            side = order.side,
            price = order.price,
            quantity = order.quantity,
            pair = order.pair,
            timestamp = order.timestamp
        )
    }
}
