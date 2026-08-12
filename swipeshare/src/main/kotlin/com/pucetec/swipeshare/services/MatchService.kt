package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchResponse
import com.pucetec.swipeshare.dto.SwipeRequest
import com.pucetec.swipeshare.dto.SwipeResponse
import com.pucetec.swipeshare.dto.SwipeType
import com.pucetec.swipeshare.dto.UpdateMatchStatusDto
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.MatchNotFoundException
import com.pucetec.swipeshare.exceptions.UnauthorizedItemAccessException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.mappers.toSwipeResponse
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val itemRepository: ItemRepository
) {

    private val logger = LoggerFactory.getLogger(MatchService::class.java)

    // Procesar la acción de Like o Dislike sobre un ítem
    fun processSwipe(cognitoId: String, request: SwipeRequest): SwipeResponse {
        val targetItem = itemRepository.findById(request.targetItemId)
            .orElseThrow { ItemNotFoundException("Target item with ID ${request.targetItemId} was not found") }

        if (targetItem.ownerId == cognitoId) {
            throw UnauthorizedItemAccessException("Access denied: You cannot swipe on your own item")
        }

        if (request.type == SwipeType.DISLIKE) {
            logger.info("event=swipe.dislike | msg=Item rejected | targetItemId={}", request.targetItemId)
            return SwipeResponse(isMatch = false, message = "Item rejected successfully")
        }

        val userItems = itemRepository.findByOwnerId(cognitoId)
        if (userItems.isEmpty()) {
            throw UnauthorizedItemAccessException("Bad request: You must create at least one item before making an offer")
        }

        val selectedOfferedItemId = when {
            request.offeredItemId != null -> {
                val belongsToUser = userItems.any { it.id == request.offeredItemId }
                if (!belongsToUser) {
                    throw UnauthorizedItemAccessException("Bad request: Offered item does not belong to you")
                }
                request.offeredItemId
            }
            userItems.size == 1 -> userItems.first().id
            else -> throw UnauthorizedItemAccessException("Bad request: Multiple items found, you must explicitly select an item to offer")
        }

        logger.info("event=swipe.like | msg=Processing like gesture | targetItemId={} offeredItemId={}",
            request.targetItemId, selectedOfferedItemId)

        val matchRequest = MatchRequest(
            offeredItemId = selectedOfferedItemId,
            requestedItemId = request.targetItemId
        )
        val matchEntity = matchRequest.toEntity(user1Id = cognitoId, user2Id = targetItem.ownerId)
        val savedMatch = matchRepository.save(matchEntity)

        return savedMatch.toSwipeResponse()
    }

    // Crear una solicitud de match directamente
    fun createMatch(cognitoId: String, request: MatchRequest): MatchResponse {
        val requestedItem = itemRepository.findById(request.requestedItemId)
            .orElseThrow { ItemNotFoundException("Requested item with ID ${request.requestedItemId} was not found") }

        val match = request.toEntity(user1Id = cognitoId, user2Id = requestedItem.ownerId)
        val savedMatch = matchRepository.save(match)
        logger.info("event=match.created | msg=Match request created | targetOwnerId={}", requestedItem.ownerId)
        return savedMatch.toResponse()
    }

    // Obtener todos los matches del usuario
    fun getMatchesByUser(cognitoId: String): List<MatchResponse> {
        return matchRepository.findByUser1IdOrUser2Id(cognitoId, cognitoId).map { it.toResponse() }
    }

    // Actualizar el estado de un match y otorgar Karma si se aprueba por primera vez
    fun updateMatchStatus(id: Long, cognitoId: String, dto: UpdateMatchStatusDto): MatchResponse {
        val match = matchRepository.findById(id)
            .orElseThrow { MatchNotFoundException("Match with ID $id was not found") }

        if (match.user1Id != cognitoId && match.user2Id != cognitoId) {
            throw UnauthorizedItemAccessException("Access denied: You are not a participant of this match")
        }

        val oldStatus = match.status
        match.status = dto.status.name
        val savedMatch = matchRepository.save(match)

        logger.info("event=match.status_updated | msg=Match status updated | matchId={} status={}", id, dto.status)

        // Otorgar +5 de Karma a ambos usuarios solo al aprobar por primera vez
        if (oldStatus != "APPROVED" && dto.status.name == "APPROVED") {
            try {
                val restTemplate = RestTemplate()
                restTemplate.postForEntity("http://users:8081/api/users/internal/${match.user1Id}/karma?amount=5", null, Void::class.java)
                restTemplate.postForEntity("http://users:8081/api/users/internal/${match.user2Id}/karma?amount=5", null, Void::class.java)
                logger.info("event=karma.awarded | msg=Awarded karma to participants | user1={} user2={}", match.user1Id, match.user2Id)
            } catch (e: Exception) {
                logger.warn("event=karma.failed | msg=Could not award karma | error=\"{}\"", e.message)
            }
        }

        return savedMatch.toResponse()
    }
}