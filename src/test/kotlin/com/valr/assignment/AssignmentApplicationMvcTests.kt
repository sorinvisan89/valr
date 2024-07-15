package com.valr.assignment

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class AssignmentApplicationMvcTests @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `should return hello`() {

        mockMvc.get("/api/orderbook/BTCUSDC")
            .andExpect {
                status { isOk() }
            }

        mockMvc.get("/api/orderbook/BTCUSDC")
            .andExpect {
                status { isOk() }
            }
    }
}
