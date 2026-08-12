package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.dto.*
import com.pucetec.swipeshare.services.MatchService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var matchService: MatchService

    private val cognitoId = "user-123"

    // Helper para simular el token con los permisos que exige Spring Security
    private fun mockJwt(subject: String = cognitoId) = jwt()
        .jwt { it.subject(subject).claim("scope", "aws.cognito.signin.user.admin") }
        .authorities(
            SimpleGrantedAuthority("ROLE_USER"),
            SimpleGrantedAuthority("SCOPE_aws.cognito.signin.user.admin")
        )

    @Test
    fun `POST swipes without token returns 401 Unauthorized`() {
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = null, type = SwipeType.LIKE)

        mockMvc.perform(
            post("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST swipes with valid token returns 200 OK`() {
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = 10L, type = SwipeType.LIKE)
        val expectedResponse = SwipeResponse(isMatch = true, message = "Match created successfully")

        `when`(matchService.processSwipe(cognitoId, request)).thenReturn(expectedResponse)

        mockMvc.perform(
            post("/api/swipes")
                .with(mockJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)
    }

    @Test
    fun `POST matches with valid token returns 201 Created`() {
        val request = MatchRequest(offeredItemId = 10L, requestedItemId = 1L)
        val expectedResponse = MatchResponse(id = 1L, user1Id = cognitoId, user2Id = "user-2", offeredItemId = 10L, requestedItemId = 1L, status = "PENDING")

        `when`(matchService.createMatch(cognitoId, request)).thenReturn(expectedResponse)

        mockMvc.perform(
            post("/api/matches")
                .with(mockJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated)
    }

    @Test
    fun `GET my matches with valid token returns 200 OK`() {
        `when`(matchService.getMatchesByUser(cognitoId)).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/matches/me")
                .with(mockJwt())
        ).andExpect(status().isOk)
    }

    @Test
    fun `PATCH update match status with valid token returns 200 OK`() {
        val request = UpdateMatchStatusDto(status = MatchStatus.APPROVED)
        val expectedResponse = MatchResponse(id = 1L, user1Id = cognitoId, user2Id = "user-2", offeredItemId = 10L, requestedItemId = 1L, status = "APPROVED")

        `when`(matchService.updateMatchStatus(1L, cognitoId, request)).thenReturn(expectedResponse)

        mockMvc.perform(
            patch("/api/matches/1/status")
                .with(mockJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)
    }
}