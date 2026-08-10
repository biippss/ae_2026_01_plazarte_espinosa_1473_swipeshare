package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.dto.ItemResponse
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.UnauthorizedItemAccessException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.ItemRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ItemService(
    private val itemRepository: ItemRepository
) {

    private val logger = LoggerFactory.getLogger(ItemService::class.java)

    fun createItem(cognitoId: String, request: ItemRequest): ItemResponse {
        logger.info("event=item.created | msg=User publishing new item | cognitoId={}", cognitoId)
        val item = request.toEntity(ownerId = cognitoId)
        val savedItem = itemRepository.save(item)
        return savedItem.toResponse()
    }

    // Retorna todos los artículos del feed excepto los creados por el usuario autenticado
    fun getAllItemsExceptUser(cognitoId: String): List<ItemResponse> {
        return itemRepository.findByOwnerIdNot(cognitoId)
            .map { it.toResponse() }
    }

    fun getItemById(id: Long): ItemResponse {
        return itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("Item with ID $id was not found in the system") }
            .toResponse()
    }

    fun deleteItem(id: Long, cognitoId: String) {
        val item = itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("Item with ID $id was not found") }

        // Validar propiedad del ítem
        if (item.ownerId != cognitoId) {
            throw UnauthorizedItemAccessException("Access denied: You do not have permission to delete an item that does not belong to you")
        }

        itemRepository.delete(item)
        logger.info("event=item.deleted | msg=Item deleted successfully | itemId={} | cognitoId={}", id, cognitoId)
    }

    fun getItemsByCognitoId(cognitoId: String): List<ItemResponse> {
        return itemRepository.findByOwnerId(cognitoId)
            .map { it.toResponse() }
    }
}