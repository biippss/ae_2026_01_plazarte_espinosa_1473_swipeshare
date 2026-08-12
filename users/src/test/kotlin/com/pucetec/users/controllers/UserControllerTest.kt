package com.pucetec.users.controllers

import com.pucetec.users.dto.UserProfileRequest
import com.pucetec.users.dto.UserProfileResponse
import com.pucetec.users.services.UserService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var userService: UserService

    private val cognitoId = "user-123"
    private val mockResponse = UserProfileResponse(1L, cognitoId, "Israel", "israel@puce.edu.ec", "Bio", "+593900000000", 10)

    // Helper para generar el JWT con las autoridades requeridas por Spring Security
    private fun mockJwt(subject: String = cognitoId) = jwt()
        .jwt { it.subject(subject).claim("scope", "aws.cognito.signin.user.admin") }
        .authorities(
            SimpleGrantedAuthority("ROLE_USER"),
            SimpleGrantedAuthority("SCOPE_aws.cognito.signin.user.admin")
        )

    @Test
    fun `GET my profile without token returns 401 Unauthorized`() {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET my profile with valid token returns 200 OK`() {
        `when`(userService.getMyProfile(cognitoId)).thenReturn(mockResponse)

        mockMvc.perform(
            get("/api/users/me")
                .with(mockJwt())
        ).andExpect(status().isOk)
    }

    @Test
    fun `POST save my profile with valid token returns 200 OK`() {
        val request = UserProfileRequest(name = "Israel", email = "israel@puce.edu.ec", bio = "Student", phone = "+593900000000")

        `when`(userService.saveOrUpdateProfile(cognitoId, "israel@puce.edu.ec", true, request))
            .thenReturn(mockResponse)

        mockMvc.perform(
            post("/api/users/me")
                .with(
                    jwt()
                        .jwt {
                            it.subject(cognitoId)
                                .claim("email", "israel@puce.edu.ec")
                                .claim("email_verified", true)
                                .claim("scope", "aws.cognito.signin.user.admin")
                        }
                        .authorities(
                            SimpleGrantedAuthority("ROLE_USER"),
                            SimpleGrantedAuthority("SCOPE_aws.cognito.signin.user.admin")
                        )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)
    }

    @Test
    fun `PUT update my profile with valid token returns 200 OK`() {
        val request = UserProfileRequest(name = "Israel Updated", email = "israel@puce.edu.ec", bio = "Student", phone = "+593900000000")

        `when`(userService.updateProfile(cognitoId, request))
            .thenReturn(mockResponse)

        mockMvc.perform(
            put("/api/users/me")
                .with(mockJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)
    }

    @Test
    fun `DELETE my profile with valid token returns 204 No Content`() {
        mockMvc.perform(
            delete("/api/users/me")
                .with(mockJwt())
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `GET profile by cognitoId returns 200 OK`() {
        `when`(userService.getProfileByCognitoId(cognitoId)).thenReturn(mockResponse)

        mockMvc.perform(
            get("/api/users/$cognitoId")
                .with(mockJwt())
        ).andExpect(status().isOk)
    }

    @Test
    fun `PATCH add karma with valid token returns 200 OK`() {
        `when`(userService.addKarma(cognitoId, 5)).thenReturn(mockResponse)

        mockMvc.perform(
            patch("/api/users/me/karma")
                .with(mockJwt())
                .param("amount", "5")
        ).andExpect(status().isOk)
    }

    @Test
    fun `POST internal karma update returns 200 OK`() {
        `when`(userService.addKarma(cognitoId, 5)).thenReturn(mockResponse)

        mockMvc.perform(
            post("/api/users/internal/$cognitoId/karma")
                .param("amount", "5")
        ).andExpect(status().isOk)
    }
}