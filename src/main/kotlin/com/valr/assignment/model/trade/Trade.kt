package com.valr.assignment.model.trade

import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.Side
import java.time.Instant

data class Trade(
    val price: Long,
    val quantity: Double,
    val currencyPair: Currency,
    val tradedAt: Instant,
    val takerSide: Side,
    val sequenceId: Long,
    val id: String,
    val quoteVolume: Double
)
