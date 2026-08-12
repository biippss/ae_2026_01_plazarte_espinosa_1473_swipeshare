package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchResponse
import com.pucetec.swipeshare.dto.SwipeRequest
import com.pucetec.swipeshare.dto.SwipeResponse
import com.pucetec.swipeshare.dto.UpdateMatchStatusDto
import com.pucetec.swipeshare.services.MatchService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class MatchController(
    private val matchService: MatchService
) {

    // 1. Procesa gestos de Like / Dislike (/api/swipes)
    @PostMapping("/swipes")
    fun processSwipe(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: SwipeRequest
    ): SwipeResponse {
        return matchService.processSwipe(jwt.subject!!, request)
    }

    // 2. Crear solicitud de match directamente (/api/matches)
    @PostMapping("/matches")
    @ResponseStatus(HttpStatus.CREATED)
    fun createMatch(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: MatchRequest
    ): MatchResponse {
        return matchService.createMatch(jwt.subject!!, request)
    }

    // 3. Obtener mis coincidencias / matches (/api/matches/me)
    @GetMapping("/matches/me")
    fun getMyMatches(
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchResponse> {
        return matchService.getMatchesByUser(jwt.subject!!)
    }

    // 4. Actualizar estado de un match (/api/matches/{id}/status)
    @PatchMapping("/matches/{id}/status")
    fun updateMatchStatus(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long,
        @RequestBody request: UpdateMatchStatusDto
    ): MatchResponse {
        return matchService.updateMatchStatus(id, jwt.subject!!, request)
    }
}