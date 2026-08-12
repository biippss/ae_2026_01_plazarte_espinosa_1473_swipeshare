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

    fun getGlobalStats(): StatsResponse {
        val approvedMatches = matchRepository.findByStatus("APPROVED")
        val matchedItemIds = approvedMatches.flatMap { match ->
            listOfNotNull(match.requestedItemId, match.offeredItemId)
        }.toSet()

        // Contar ítems disponibles en la plataforma (no intercambiados)
        val activeItemsCount = itemRepository.findAll().count { item -> !matchedItemIds.contains(item.id) }
        val totalMatchesCount = approvedMatches.size.toLong()

        logger.info("event=public.stats_viewed | msg=Global statistics calculated")
        return StatsResponse(
            totalItems = activeItemsCount.toLong(),
            totalMatches = totalMatchesCount
        )
    }
}