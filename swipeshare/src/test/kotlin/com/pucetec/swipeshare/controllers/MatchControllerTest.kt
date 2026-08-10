package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.services.MatchService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var matchService: MatchService

    @Test
    fun `GET my matches without token returns 401 Unauthorized`() {
        mockMvc.perform(get("/swipeshare/matches/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET my matches with valid token returns 200 OK`() {
        val cognitoId = "user-123"
        `when`(matchService.getMatchesByUser(cognitoId)).thenReturn(emptyList())

        mockMvc.perform(get("/swipeshare/matches/me").with(jwt().jwt { it.subject(cognitoId) }))
            .andExpect(status().isOk)
    }
}