package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchResponse
import com.pucetec.swipeshare.services.MatchService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
class MatchController(
    val matchService: MatchService
) {
    private val logger = LoggerFactory.getLogger(MatchController::class.java)

    @PostMapping("/api/matches")
    fun createMatch(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: MatchRequest
    ): MatchResponse {
        val cognitoId = jwt.subject!!
        logger.info("User $cognitoId registering a swipe/match")
        return matchService.createMatch(cognitoId, request)
    }

    @GetMapping("/api/matches/me")
    fun getMyMatches(
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchResponse> {
        val cognitoId = jwt.subject!!
        logger.info("Getting matches for user $cognitoId")
        return matchService.getMatchesByUser(cognitoId)
    }

    @PutMapping("/api/matches/{id}/status")
    fun updateMatchStatus(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long,
        @RequestParam status: String
    ): MatchResponse {
        val cognitoId = jwt.subject!!
        logger.info("User $cognitoId updating match $id status to $status")
        // Llama a tu MatchService para cambiar el status en tu tabla 'matches' existente
        return matchService.updateMatchStatus(id, cognitoId, status)
    }
}