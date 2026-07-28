package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.UserRequest
import com.pucetec.swipeshare.dto.UserResponse
import com.pucetec.swipeshare.dto.StatsResponse
import com.pucetec.swipeshare.exceptions.UserNotFoundException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.UserRepository
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val itemRepository: ItemRepository,
    private val matchRepository: MatchRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun createUser(cognitoId: String, request: UserRequest): UserResponse {
        logger.info("Guardando perfil local para el usuario de Cognito: $cognitoId")
        val user = request.toEntity(cognitoId)
        return userRepository.save(user).toResponse()
    }

    fun getUserByCognitoId(cognitoId: String): UserResponse {
        logger.info("Buscando usuario por su id de Cognito: $cognitoId")
        return userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario con cognitoId $cognitoId no registrado") }
            .toResponse()
    }

    fun updateUser(cognitoId: String, request: UserRequest): UserResponse {
        logger.info("Actualizando datos del usuario: $cognitoId")
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado") }

        user.name = request.name
        user.email = request.email
        user.phone = request.phone
        return userRepository.save(user).toResponse()
    }

    fun getAllUsers(): List<UserResponse> {
        logger.info("Obteniendo catálogo completo de estudiantes")
        return userRepository.findAll().map { it.toResponse() }
    }

    fun getUserById(id: Long): UserResponse {
        return userRepository.findById(id)
            .orElseThrow { UserNotFoundException("No existe el usuario con ID: $id") }
            .toResponse()
    }

    fun deleteUser(id: Long) {
        logger.info("Eliminando de forma lógica/física al usuario: $id")
        val user = userRepository.findById(id)
            .orElseThrow { UserNotFoundException("No se pudo eliminar, usuario inexistente") }
        userRepository.delete(user)
    }

    fun getUserHistory(cognitoId: String): Map<String, Any> {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado") }

        // Buscamos todas sus interacciones en la base de datos existente
        val userMatches = matchRepository.findByUser1IdOrUser2Id(user.id, user.id)

        return mapOf(
            "userId" to user.id,
            "studentName" to user.name,
            "currentKarma" to user.karmaBalance,
            "totalInteractions" to userMatches.size,
            "matchesHistory" to userMatches.map { mapOf("matchId" to it.id, "status" to it.status) }
        )
    }

    // Para dar soporte a tu PublicController y ver contadores en la app
    fun getGlobalStats(): StatsResponse {
        val totalUsers = userRepository.count().toInt()
        val totalItems = itemRepository.count().toInt()
        val activeMatches = matchRepository.countByStatus("ACCEPTED").toInt()
        return StatsResponse(totalUsers, totalItems, activeMatches)
    }
}