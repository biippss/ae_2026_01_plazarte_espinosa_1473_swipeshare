package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.dto.ItemResponse
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.UnauthorizedItemAccessException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val matchRepository: MatchRepository
) {

    private val logger = LoggerFactory.getLogger(ItemService::class.java)

    // Publica un nuevo ítem en la plataforma
    fun createItem(cognitoId: String, request: ItemRequest): ItemResponse {
        val item = request.toEntity(ownerId = cognitoId)
        val savedItem = itemRepository.save(item)
        logger.info("event=item.created | msg=New item published | itemId={} category=\"{}\"", savedItem.id, savedItem.category)
        return savedItem.toResponse()
    }

    // Retorna los artículos del feed excluyendo los propios y los ya intercambiados (APPROVED)
    fun getAllItemsExceptUser(cognitoId: String): List<ItemResponse> {
        val availableItems = itemRepository.findByOwnerIdNot(cognitoId)
        val approvedMatches = matchRepository.findByStatus("APPROVED")

        val matchedItemIds = approvedMatches.flatMap { match ->
            listOfNotNull(match.requestedItemId, match.offeredItemId)
        }.toSet()

        return availableItems
            .filterNot { item -> matchedItemIds.contains(item.id) }
            .map { it.toResponse() }
    }

    // Obtiene un ítem por su ID
    fun getItemById(id: Long): ItemResponse {
        return itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("Item with ID $id was not found") }
            .toResponse()
    }

    // Elimina un ítem si pertenece al usuario autenticado
    fun deleteItem(id: Long, cognitoId: String) {
        val item = itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("Item with ID $id was not found") }

        if (item.ownerId != cognitoId) {
            throw UnauthorizedItemAccessException("Access denied: You do not have permission to delete this item")
        }

        itemRepository.delete(item)
        logger.info("event=item.deleted | msg=Item deleted successfully | itemId={}", id)
    }

    // Obtiene todos los ítems publicados por el usuario autenticado
    fun getItemsByCognitoId(cognitoId: String): List<ItemResponse> {
        return itemRepository.findByOwnerId(cognitoId)
            .map { it.toResponse() }
    }

    // Actualiza la información de un ítem existente
    fun updateItem(id: Long, cognitoId: String, request: ItemRequest): ItemResponse {
        val item = itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("Item with ID $id was not found") }

        if (item.ownerId != cognitoId) {
            throw UnauthorizedItemAccessException("Access denied: You do not have permission to modify this product")
        }

        item.title = request.title
        item.description = request.description
        item.category = request.category
        item.imageUrl = request.imageUrl

        val updatedItem = itemRepository.save(item)
        logger.info("event=item.updated | msg=Item updated successfully | itemId={}", id)
        return updatedItem.toResponse()
    }
}