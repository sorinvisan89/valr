package com.valr.assignment

import com.valr.assignment.dto.AuthenticationRequestDTO
import com.valr.assignment.dto.AuthenticationResponseDTO
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class SecurityIntegrationTest : AbstractContainerSetup() {

    @Test
    fun `authentication with correct user and password works successfully`() {
        val authRequest = AuthenticationRequestDTO(email = "first@gmail.com", password = "pass1")
        val authResponse = restTemplate.postForEntity(
            "http://localhost:${appContainer.getMappedPort(8081)}/api/auth",
            HttpEntity(authRequest, HttpHeaders()),
            AuthenticationResponseDTO::class.java
        )

        authResponse.statusCode.value() shouldBe HttpStatus.OK.value()
        authResponse.body shouldNotBe null
        authResponse.body!!.accessToken shouldNotBe null
    }

    @Test
    fun `authentication with incorrect credentials`() {
        val authRequest = AuthenticationRequestDTO(email = "invali@gmail.com", password = "invalid")
        val authResponse = restTemplate.postForEntity(
            "http://localhost:${appContainer.getMappedPort(8081)}/api/auth",
            HttpEntity(authRequest, HttpHeaders()),
            AuthenticationResponseDTO::class.java
        )

        authResponse.statusCode.value() shouldBe HttpStatus.FORBIDDEN.value()
    }
}
