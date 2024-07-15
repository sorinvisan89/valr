package com.valr.assignment.controller

import com.valr.assignment.api.OrderBookManagerApi
import com.valr.assignment.dto.ErrorDTO
import com.valr.assignment.dto.OrderBookDTO
import com.valr.assignment.dto.OrderDTO
import com.valr.assignment.dto.OrderRequestDTO
import com.valr.assignment.dto.TradeDTO
import com.valr.assignment.model.currency.Currency
import com.valr.assignment.service.OrderBookManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderBookManagerController(private val orderBookManager: OrderBookManager) :
    OrderBookManagerApi {

    override fun getOrderBook(input: String): ResponseEntity<out Any> {
        val currency = Currency.fromString(input) ?: return badCurrencyResponse(input)
        val orderBook = orderBookManager.getOrderBook(currency)
        return ResponseEntity.ok(OrderBookDTO.fromOrderBook(orderBook))
    }

    override fun placeLimitOrder(orderRequest: OrderRequestDTO): ResponseEntity<OrderDTO> {
        val order = orderRequest.toOrder()
        val placedOrder = orderBookManager.placeLimitOrder(order)
        return ResponseEntity.ok(OrderDTO.fromOrder(placedOrder))
    }

    override fun getRecentTrades(input: String): ResponseEntity<out Any> {
        val currency = Currency.fromString(input) ?: return badCurrencyResponse(input)
        val trades = orderBookManager.getRecentTrades(currency)
        return ResponseEntity.ok(trades.map { TradeDTO.fromTrade(it) })
    }

    private fun badCurrencyResponse(input: String): ResponseEntity<ErrorDTO> =
        ResponseEntity.badRequest().body(ErrorDTO("Failed to parse currency '$input'"))
}
