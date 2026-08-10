package com.pucetec.users.controllers

import com.pucetec.users.dto.UserProfileResponse
import com.pucetec.users.services.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

    @Test
    fun `GET my profile without token returns 401 Unauthorized`() {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET my profile with a valid token returns 200 OK`() {
        val cognitoId = "user-123"
        val expectedProfile = UserProfileResponse(1L, cognitoId, "Israel", "israel@puce.edu.ec", null, null, 10)

        `when`(userService.getMyProfile(cognitoId)).thenReturn(expectedProfile)

        mockMvc.perform(
            get("/api/users/me")
                .with(
                    jwt()
                        .jwt { it.subject(cognitoId).claim("scope", "aws.cognito.signin.user.admin") }
                        .authorities(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("SCOPE_aws.cognito.signin.user.admin"))
                )
        ).andExpect(status().isOk)
    }
}