package com.valr.assignment.service

import com.valr.assignment.dto.AuthenticationRequestDTO
import com.valr.assignment.dto.AuthenticationResponseDTO
import com.valr.assignment.repository.RefreshTokenRepository
import com.valr.assignment.security.JwtProperties
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    fun authentication(authenticationRequest: AuthenticationRequestDTO): AuthenticationResponseDTO {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.email,
                authenticationRequest.password
            )
        )

        val user = userDetailsService.loadUserByUsername(authenticationRequest.email)

        val accessToken = createAccessToken(user)
        val refreshToken = createRefreshToken(user)

        refreshTokenRepository.save(refreshToken, user)

        return AuthenticationResponseDTO(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun refreshAccessToken(refreshToken: String): String? {
        val extractedEmail = tokenService.extractEmail(refreshToken)

        return extractedEmail?.let { email ->
            val currentUserDetails = userDetailsService.loadUserByUsername(email)
            val refreshTokenUserDetails = refreshTokenRepository.findUserDetailsByToken(refreshToken)

            if (!tokenService.isExpired(refreshToken) && refreshTokenUserDetails?.username == currentUserDetails.username)
                createAccessToken(currentUserDetails)
            else
                null
        }
    }

    private fun createAccessToken(user: UserDetails) = tokenService.generate(
        userDetails = user,
        expirationDate = getAccessTokenExpiration()
    )

    private fun createRefreshToken(user: UserDetails) = tokenService.generate(
        userDetails = user,
        expirationDate = getRefreshTokenExpiration()
    )

    private fun getAccessTokenExpiration(): Date =
        Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)

    private fun getRefreshTokenExpiration(): Date =
        Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration)
}
