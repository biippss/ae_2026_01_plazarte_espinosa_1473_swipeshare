package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchResponse
import com.pucetec.swipeshare.exceptions.MatchNotFoundException
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.MatchRepository
import com.pucetec.swipeshare.repositories.ItemRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val itemRepository: ItemRepository
) {

    private val logger = LoggerFactory.getLogger(MatchService::class.java)

    fun createMatch(cognitoId: String, request: MatchRequest): MatchResponse {
        val requestedItem = itemRepository.findById(request.requestedItemId)
            .orElseThrow { ItemNotFoundException("El objeto solicitado no existe") }

        logger.info("Usuario $cognitoId solicita intercambio al dueño ${requestedItem.ownerId}")

        // Inyectamos el cognitoId del solicitante y el ownerId del item solicitado
        val match = request.toEntity(user1Id = cognitoId, user2Id = requestedItem.ownerId)
        return matchRepository.save(match).toResponse()
    }

    fun getMatchesByUser(cognitoId: String): List<MatchResponse> {
        return matchRepository.findByUser1IdOrUser2Id(cognitoId, cognitoId).map { it.toResponse() }
    }

    fun updateMatchStatus(id: Long, cognitoId: String, status: String): MatchResponse {
        val match = matchRepository.findById(id)
            .orElseThrow { MatchNotFoundException("Match con ID $id no existente") }

        logger.info("Cambiando el estado del match $id a: $status")
        match.status = status
        return matchRepository.save(match).toResponse()
    }
}