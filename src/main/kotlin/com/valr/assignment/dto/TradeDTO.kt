package com.valr.assignment.dto

import com.valr.assignment.model.currency.Currency
import com.valr.assignment.model.order.Side
import com.valr.assignment.model.trade.Trade
import java.time.Instant

data class TradeDTO(
    val id: String,
    val price: Long,
    val quantity: Double,
    val currencyPair: Currency,
    val tradedAt: Instant,
    val takerSide: Side,
    val sequenceId: Long,
    val quoteVolume: Double
) {

    companion object {
        fun fromTrade(trade: Trade) = TradeDTO(
            id = trade.id,
            price = trade.price,
            quantity = trade.quantity,
            currencyPair = trade.currencyPair,
            tradedAt = trade.tradedAt,
            takerSide = trade.takerSide,
            sequenceId = trade.sequenceId,
            quoteVolume = trade.quoteVolume
        )
    }
}
