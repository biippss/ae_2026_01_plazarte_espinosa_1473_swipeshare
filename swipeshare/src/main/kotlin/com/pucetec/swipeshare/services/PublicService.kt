package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.StatsResponse
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PublicService(
    private val itemRepository: ItemRepository,
    private val matchRepository: MatchRepository
) {
    private val logger = LoggerFactory.getLogger(PublicService::class.java)

    // Obtiene las estadísticas públicas globales del sistema
    fun getGlobalStats(): StatsResponse {
        val totalItems = itemRepository.count()
        val totalMatches = matchRepository.count()
        logger.info("event=public.stats_viewed | msg=Global statistics calculated")
        return StatsResponse(
            totalItems = totalItems,
            totalMatches = totalMatches
        )
    }
}