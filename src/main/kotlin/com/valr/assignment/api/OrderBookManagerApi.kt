package com.valr.assignment.api

import com.valr.assignment.dto.ErrorDTO
import com.valr.assignment.dto.OrderBookDTO
import com.valr.assignment.dto.OrderDTO
import com.valr.assignment.dto.OrderRequestDTO
import com.valr.assignment.dto.TradeDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "OrderBookManager", description = "APIs for managing order books and trades")
@RequestMapping("/api/orderbook")
interface OrderBookManagerApi {

    @Operation(
        summary = "Get order book for a specific currency",
        description = "Retrieve the order book containing buy and sell orders for a given currency.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the order book",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OrderBookDTO::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid currency code",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorDTO::class)
                    )
                ]
            )
        ]
    )
    @GetMapping("/{currency}")
    fun getOrderBook(
        @Parameter(description = "Currency code", required = true)
        @PathVariable(value = "currency") input: String
    ): ResponseEntity<out Any>

    @Operation(
        summary = "Place a limit order",
        description = "Submit a new limit order to the order book.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Details of the order to be placed",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = OrderRequestDTO::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully placed the order",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OrderDTO::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid order details",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorDTO::class)
                    )
                ]
            )
        ]
    )
    @PostMapping("/orders")
    fun placeLimitOrder(
        @Parameter(description = "Order request", required = true)
        @Validated @RequestBody orderRequest: OrderRequestDTO
    ): ResponseEntity<OrderDTO>

    @Operation(
        summary = "Get recent trades for a specific currency",
        description = "Retrieve the most recent trades executed for a given currency.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved recent trades",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TradeDTO::class, type = "array")
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid currency code",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorDTO::class)
                    )
                ]
            )
        ]
    )
    @GetMapping("/trades/{currency}")
    fun getRecentTrades(
        @Parameter(description = "Currency code", required = true)
        @PathVariable(value = "currency") input: String
    ): ResponseEntity<out Any>
}
