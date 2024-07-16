package com.valr.assignment.controller

import com.valr.assignment.api.OrderBookManagerApi
import com.valr.assignment.dto.ErrorDTO
import com.valr.assignment.dto.OrderBookDTO
import com.valr.assignment.dto.OrderDTO
import com.valr.assignment.dto.OrderRequestDTO
import com.valr.assignment.dto.TradeDTO
import com.valr.assignment.model.currency.Currency
import com.valr.assignment.service.OrderBookService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderBookManagerController(private val orderBookService: OrderBookService) :
    OrderBookManagerApi {

    override fun getOrderBook(input: String): ResponseEntity<out Any> {
        val currency = Currency.fromString(input) ?: return badCurrencyResponse(input)
        val orderBook = orderBookService.getOrderBook(currency)
        return ResponseEntity.ok(OrderBookDTO.fromOrderBook(orderBook))
    }

    override fun placeLimitOrder(orderRequest: OrderRequestDTO): ResponseEntity<OrderDTO> {
        val order = orderRequest.toOrder()
        val placedOrder = orderBookService.placeLimitOrder(order)
        return ResponseEntity.ok(OrderDTO.fromOrder(placedOrder))
    }

    override fun getRecentTrades(input: String): ResponseEntity<out Any> {
        val currency = Currency.fromString(input) ?: return badCurrencyResponse(input)
        val trades = orderBookService.getRecentTrades(currency)
        return ResponseEntity.ok(trades.map { TradeDTO.fromTrade(it) })
    }

    private fun badCurrencyResponse(input: String): ResponseEntity<ErrorDTO> =
        ResponseEntity.badRequest().body(ErrorDTO("Failed to parse currency '$input'"))
}
