package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.dto.UserRequest
import com.pucetec.swipeshare.dto.UserResponse
import com.pucetec.swipeshare.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    val userService: UserService
) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    // ============================================================
    // Endpoints del propio usuario.
    // Spring extrae el "sub" (cognitoId) desde el token validado.
    // ============================================================

    @PostMapping("/api/users/me")
    fun createMyProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserRequest
    ): UserResponse {
        val cognitoId = jwt.subject
        logger.info("Creating profile for authenticated user $cognitoId")
        return userService.createUser(cognitoId, request)
    }

    @GetMapping("/api/users/me")
    fun getMyProfile(
        @AuthenticationPrincipal jwt: Jwt
    ): UserResponse {
        val cognitoId = jwt.subject
        logger.info("Getting profile for authenticated user $cognitoId")
        return userService.getUserByCognitoId(cognitoId)
    }

    @PutMapping("/api/users/me")
    fun updateMyProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserRequest
    ): UserResponse {
        val cognitoId = jwt.subject
        logger.info("Updating profile for authenticated user $cognitoId")
        return userService.updateUser(cognitoId, request)
    }

    @GetMapping("/api/users/me/history")
    fun getMyHistory(
        @AuthenticationPrincipal jwt: Jwt
    ): Map<String, Any> {
        val cognitoId = jwt.subject
        logger.info("Fetching combined activity history for user $cognitoId")

        // Delegamos la lógica al UserService para mantener el controlador limpio
        return userService.getUserHistory(cognitoId)
    }

    // ============================================================
    // Endpoints administrativos y de consulta general
    // ============================================================

    @GetMapping("/api/users")
    fun getAllUsers(): List<UserResponse> {
        logger.info("Getting all users")
        return userService.getAllUsers()
    }

    @GetMapping("/api/users/{id}")
    fun getUserById(@PathVariable id: Long): UserResponse {
        logger.info("Getting user with id: $id")
        return userService.getUserById(id)
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long) {
        logger.info("Deleting user $id")
        userService.deleteUser(id)
    }
}