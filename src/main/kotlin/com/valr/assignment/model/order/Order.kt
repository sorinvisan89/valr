package com.valr.assignment.model.order

import com.valr.assignment.model.currency.Currency
import java.time.Instant
import java.util.*

data class Order(
    val id: String,
    val side: Side,
    var quantity: Double,
    val price: Long,
    val pair: Currency,
    val timestamp: Instant = Instant.now()
)