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

    // 1. Obtener mi perfil
    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal jwt: Jwt): UserProfileResponse {
        val cognitoId = jwt.subject!!
        return userService.getMyProfile(cognitoId)
    }

    // 2. Crear mi perfil por primera vez
    @PostMapping("/me")
    fun saveMyProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserProfileRequest
    ): UserProfileResponse {
        val cognitoId = jwt.subject!!
        return userService.saveOrUpdateProfile(cognitoId, request)
    }

    // 3. Actualizar mi perfil (PUT)
    @PutMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserProfileRequest
    ): UserProfileResponse {
        val cognitoId = jwt.subject!!
        return userService.updateProfile(cognitoId, request)
    }

    // 4. Eliminar mi perfil (DELETE)
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMyProfile(
        @AuthenticationPrincipal jwt: Jwt
    ) {
        val cognitoId = jwt.subject!!
        userService.deleteProfile(cognitoId)
    }

    // 5. Consultar perfil público de otro usuario
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
    @PutMapping("/internal/{cognitoId}/karma")
    fun updateKarma(
        @PathVariable cognitoId: String,
        @RequestParam amount: Int
    ): UserProfileResponse {
        return userService.addKarma(cognitoId, amount)
    }
}