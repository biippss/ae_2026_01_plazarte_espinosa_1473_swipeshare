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
        logger.info("El usuario $cognitoId está publicando un nuevo artículo")
        val item = request.toEntity(ownerId = cognitoId)
        return itemRepository.save(item).toResponse()
    }

    fun getAllItems(): List<ItemResponse> {
        return itemRepository.findAll().map { it.toResponse() }
    }

    fun getItemById(id: Long): ItemResponse {
        return itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("El objeto con ID $id no existe en el sistema") }
            .toResponse()
    }

    fun deleteItem(id: Long, cognitoId: String) {
        val item = itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("El objeto con ID $id no existe") }

        // Validar propiedad del objeto
        if (item.ownerId != cognitoId) {
            throw UnauthorizedItemAccessException("Acceso denegado: No tienes permiso para borrar un objeto que no te pertenece")
        }

        itemRepository.delete(item)
        logger.info("Objeto $id eliminado correctamente por el usuario $cognitoId")
    }

    fun getItemsByCognitoId(cognitoId: String): List<ItemResponse> {
        return itemRepository.findByOwnerId(cognitoId).map { it.toResponse() }
    }
}