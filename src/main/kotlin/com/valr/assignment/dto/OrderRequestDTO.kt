package com.valr.assignment.dto

import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.Order
import com.valr.assignment.model.order.Side
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class OrderRequestDTO(
    @field:NotNull(message = "Side must be specified") val side: Side,
    @field:Min(0) val price: Long,
    @field:Min(0) val quantity: Double,
    @field:NotNull
    val pair: Currency,
) {

    fun toOrder() = Order(
        id = UUID.randomUUID().toString(),
        side = side,
        price = price,
        quantity = quantity,
        pair = pair,
        timestamp = Instant.now()
    )
}
