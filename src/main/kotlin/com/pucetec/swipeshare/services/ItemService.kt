package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.dto.ItemResponse
import com.pucetec.swipeshare.entities.Item
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.UserNotFoundException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(ItemService::class.java)

    fun createItem(cognitoId: String, request: ItemRequest): ItemResponse {
        logger.info("El estudiante de Cognito $cognitoId está publicando un objeto")
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Debes tener un perfil registrado para publicar objetos") }

        val item = request.toEntity(user.id)
        return itemRepository.save(item).toResponse()
    }

    fun getAllItems(): List<ItemResponse> {
        return itemRepository.findAll().map { it.toResponse() }
    }

    fun getItemById(id: Long): ItemResponse {
        return itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("El objeto con id $id no existe en el sistema") }
            .toResponse()
    }

    fun deleteItem(id: Long, cognitoId: String) {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no válido") }

        val item = itemRepository.findById(id)
            .orElseThrow { ItemNotFoundException("Objeto no encontrado") }

        // Validación de Seguridad de Negocio idéntica al ejemplo de tu clase
        if (item.ownerId != user.id) {
            throw RuntimeException("Acceso denegado: No puedes borrar un objeto que no te pertenece")
        }

        itemRepository.delete(item)
        logger.info("Objeto $id eliminado correctamente por su dueño")
    }

    fun getItemsByCognitoId(cognitoId: String): List<ItemResponse> {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado") }
        return itemRepository.findByOwnerId(user.id).map { it.toResponse() }
    }
}