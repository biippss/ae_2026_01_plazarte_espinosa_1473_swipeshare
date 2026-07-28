package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchResponse
import com.pucetec.swipeshare.exceptions.MatchNotFoundException
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.UserNotFoundException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.MatchRepository
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(MatchService::class.java)

    fun createMatch(cognitoId: String, request: MatchRequest): MatchResponse {
        val user1 = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario origen no registrado") }

        val requestedItem = itemRepository.findById(request.requestedItemId)
            .orElseThrow { ItemNotFoundException("El objeto solicitado no existe") }

        logger.info("Usuario ${user1.id} solicita intercambio al dueño del item ${requestedItem.id}")

        // Generamos la relación apuntando a los ID internos de los dos estudiantes
        val match = request.toEntity(user1Id = user1.id, user2Id = requestedItem.ownerId)
        return matchRepository.save(match).toResponse()
    }

    fun getMatchesByUser(cognitoId: String): List<MatchResponse> {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado") }
        return matchRepository.findByUser1IdOrUser2Id(user.id, user.id).map { it.toResponse() }
    }

    fun updateMatchStatus(id: Long, cognitoId: String, status: String): MatchResponse {
        val match = matchRepository.findById(id)
            .orElseThrow { MatchNotFoundException("Match con ID $id no existente") }

        logger.info("Cambiando el estado del match $id a: $status")
        match.status = status
        return matchRepository.save(match).toResponse()
    }
}