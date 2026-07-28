package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.dto.StatsResponse
import com.pucetec.swipeshare.services.PublicService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PublicController(
    val publicService: PublicService
) {

    private val logger = LoggerFactory.getLogger(PublicController::class.java)

    // ============================================================
    // Endpoint público (Sin token). Muestra las estadísticas generales.
    // ============================================================
    @GetMapping("/api/public/stats")
    fun getSystemStats(): StatsResponse {
        logger.info("Getting public system statistics")
        return publicService.getGlobalStats()
    }
}