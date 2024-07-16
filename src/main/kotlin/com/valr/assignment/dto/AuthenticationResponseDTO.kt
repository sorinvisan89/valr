package com.valr.assignment.dto

data class AuthenticationResponseDTO(
    val accessToken: String,
    val refreshToken: String,
)
