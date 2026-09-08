package com.teamnative.backend.global.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `public endpoint is permitted`() {
        mockMvc.perform(get("/api/v1/public/ping"))
            .andExpect(status().isOk)
    }

    @Test
    fun `private endpoint requires authentication`() {
        mockMvc.perform(get("/api/v1/private/ping"))
            .andExpect(status().isUnauthorized)
    }
}
