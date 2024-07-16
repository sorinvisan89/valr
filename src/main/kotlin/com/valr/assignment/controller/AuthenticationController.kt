package com.valr.assignment.controller

import com.valr.assignment.api.AuthApi
import com.valr.assignment.dto.AuthenticationRequestDTO
import com.valr.assignment.dto.AuthenticationResponseDTO
import com.valr.assignment.dto.RefreshTokenRequestDTO
import com.valr.assignment.dto.TokenResponseDTO
import com.valr.assignment.service.AuthenticationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(private val authenticationService: AuthenticationService) : AuthApi {

    override fun authenticate(authRequest: AuthenticationRequestDTO): AuthenticationResponseDTO =
        authenticationService.authentication(authRequest)

    override fun refreshAccessToken(request: RefreshTokenRequestDTO): TokenResponseDTO =
        authenticationService.refreshAccessToken(request.token)
            ?.mapToTokenResponse()
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token.")

    private fun String.mapToTokenResponse(): TokenResponseDTO =
        TokenResponseDTO(
            token = this
        )
}
