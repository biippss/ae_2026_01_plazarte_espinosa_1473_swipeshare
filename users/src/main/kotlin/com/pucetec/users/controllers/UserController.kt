package com.pucetec.users.controllers

import com.pucetec.users.dto.UserProfileRequest
import com.pucetec.users.dto.UserProfileResponse
import com.pucetec.users.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal jwt: Jwt): UserProfileResponse {
        val cognitoId = jwt.subject!!
        return userService.getMyProfile(cognitoId)
    }

    @PostMapping("/me")
    fun saveMyProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserProfileRequest
    ): UserProfileResponse {
        val cognitoId = jwt.subject ?: throw IllegalArgumentException("Token inválido: No contiene subject (sub).")
        val email = jwt.claims["email"]?.toString() ?: ""

        val isEmailVerified = when (val claim = jwt.claims["email_verified"]) {
            is Boolean -> claim
            is String -> claim.toBoolean()
            else -> false
        }

        return userService.saveOrUpdateProfile(
            cognitoId = cognitoId,
            emailFromJwt = email,
            isEmailVerified = isEmailVerified,
            request = request
        )
    }

    @PutMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserProfileRequest
    ): UserProfileResponse {
        val cognitoId = jwt.subject!!
        return userService.updateProfile(cognitoId, request)
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMyProfile(
        @AuthenticationPrincipal jwt: Jwt
    ) {
        val cognitoId = jwt.subject!!
        userService.deleteProfile(cognitoId)
    }

    @GetMapping("/{cognitoId}")
    fun getProfileByCognitoId(@PathVariable cognitoId: String): UserProfileResponse {
        return userService.getProfileByCognitoId(cognitoId)
    }

    @PatchMapping("/me/karma")
    fun addKarma(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam amount: Int
    ): UserProfileResponse {
        val cognitoId = jwt.subject!!
        return userService.addKarma(cognitoId, amount)
    }

    // CORREGIDO: @PostMapping para recibir la llamada interna de MatchService
    @PostMapping("/internal/{cognitoId}/karma")
    fun updateKarma(
        @PathVariable cognitoId: String,
        @RequestParam amount: Int
    ): UserProfileResponse {
        return userService.addKarma(cognitoId, amount)
    }
}