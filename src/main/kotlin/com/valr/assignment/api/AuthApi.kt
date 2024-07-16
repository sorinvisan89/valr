package com.valr.assignment.api

import com.valr.assignment.dto.AuthenticationRequestDTO
import com.valr.assignment.dto.AuthenticationResponseDTO
import com.valr.assignment.dto.RefreshTokenRequestDTO
import com.valr.assignment.dto.TokenResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "Authentication", description = "Authentication API")
@RequestMapping("/api/auth")
interface AuthApi {

    @Operation(
        summary = "Authenticate user",
        description = "Authenticate user with credentials",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully authenticated",
                content = [Content(schema = Schema(implementation = AuthenticationResponseDTO::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = String::class))]
            )
        ]
    )
    @PostMapping
    fun authenticate(
        @Parameter(description = "Authentication Request", required = true)
        @RequestBody authRequest: AuthenticationRequestDTO
    ): AuthenticationResponseDTO

    @Operation(
        summary = "Refresh access token",
        description = "Refresh the access token using the refresh token",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = [Content(schema = Schema(implementation = TokenResponseDTO::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Invalid refresh token",
                content = [Content(schema = Schema(implementation = String::class))]
            )
        ]
    )
    @PostMapping("/refresh")
    fun refreshAccessToken(
        @Parameter(description = "Refresh Token Request", required = true)
        @RequestBody request: RefreshTokenRequestDTO
    ): TokenResponseDTO
}
