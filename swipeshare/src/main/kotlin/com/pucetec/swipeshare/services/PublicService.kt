package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.StatsResponse
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.springframework.stereotype.Service

@Service
class PublicService(
    private val itemRepository: ItemRepository,
    private val matchRepository: MatchRepository
) {
    fun getGlobalStats(): StatsResponse {
        val totalItems = itemRepository.count()
        val totalMatches = matchRepository.count()
        return StatsResponse(
            totalItems = totalItems,
            totalMatches = totalMatches
        )
    }
}