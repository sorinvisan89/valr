package com.valr.assignment.controller

import com.valr.assignment.dto.*
import com.valr.assignment.model.currency.Currency
import com.valr.assignment.service.OrderBookManager
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orderbook")
class OrderBookManagerController(private val orderBookManager: OrderBookManager) {

    @GetMapping("/{currency}")
    fun getOrderBook(@PathVariable(value = "currency") input: String): ResponseEntity<out Any> {
        val currency =
            Currency.fromString(input) ?: return badCurrencyResponse(input)
        val orderBook = orderBookManager.getOrderBook(currency)
        return ResponseEntity.ok(OrderBookDTO.fromOrderBook(orderBook))
    }

    @PostMapping("/orders")
    fun placeLimitOrder(@Validated @RequestBody orderRequest: OrderRequestDTO): ResponseEntity<OrderDTO> {
        val order = orderRequest.toOrder()
        val placedOrder = orderBookManager.placeLimitOrder(order)
        return ResponseEntity.ok(OrderDTO.fromOrder(placedOrder))
    }

    @GetMapping("/trades/{currency}")
    fun getRecentTrades(@PathVariable(value = "currency") input: String): ResponseEntity<out Any> {
        val currency =
            Currency.fromString(input) ?: return badCurrencyResponse(input)
        val trades = orderBookManager.getRecentTrades(currency)
        return ResponseEntity.ok(trades.map { TradeDTO.fromTrade(it) })
    }


    private fun badCurrencyResponse(input: String) =
        ResponseEntity.badRequest().body(ErrorDTO("Failed to parse currency '$input'"))


}