package com.pucetec.users.services

import com.pucetec.users.dto.UserProfileRequest
import com.pucetec.users.dto.UserProfileResponse
import com.pucetec.users.exceptions.BlankNameException
import com.pucetec.users.exceptions.UserNotFoundException
import com.pucetec.users.mappers.toEntity
import com.pucetec.users.mappers.toResponse
import com.pucetec.users.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * 1. Obtiene el perfil del usuario autenticado.
     */
    fun getMyProfile(cognitoId: String): UserProfileResponse {
        return userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado con ID de Cognito: $cognitoId") }
            .toResponse()
    }

    /**
     * 2. Crea el perfil si no existe, o lo actualiza si ya existe.
     */
    fun saveOrUpdateProfile(cognitoId: String, request: UserProfileRequest): UserProfileResponse {
        logger.info("Guardando o creando perfil para Cognito ID: $cognitoId")

        // Validar que el nombre no venga vacío
        if (request.name.isBlank()) {
            throw BlankNameException("El nombre no puede estar vacío")
        }

        val existingUser = userRepository.findByCognitoId(cognitoId)
        if (existingUser.isPresent) {
            val user = existingUser.get()
            user.name = request.name
            user.email = request.email
            user.bio = request.bio
            user.phone = request.phone
            return userRepository.save(user).toResponse()
        }

        val newUser = request.toEntity(cognitoId = cognitoId)
        return userRepository.save(newUser).toResponse()
    }

    /**
     * 3. Actualiza mi perfil.
     */
    fun updateProfile(cognitoId: String, request: UserProfileRequest): UserProfileResponse {
        // Validar que el nombre no venga vacío
        if (request.name.isBlank()) {
            throw BlankNameException("El nombre no puede estar vacío")
        }

        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado con ID de Cognito: $cognitoId") }

        user.name = request.name
        user.email = request.email
        user.bio = request.bio
        user.phone = request.phone

        logger.info("Perfil actualizado correctamente para el usuario: $cognitoId")
        return userRepository.save(user).toResponse()
    }

    /**
     * 4. Elimina mi perfil.
     */
    fun deleteProfile(cognitoId: String) {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado con ID de Cognito: $cognitoId") }

        userRepository.delete(user)
        logger.info("Usuario $cognitoId eliminado del sistema")
    }

    /**
     * 5. Consultar perfil público de otro usuario por su Cognito ID.
     */
    fun getProfileByCognitoId(cognitoId: String): UserProfileResponse {
        return userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado con ID de Cognito: $cognitoId") }
            .toResponse()
    }

    /**
     * Agrega o resta Karma de forma segura.
     * Mantiene la regla de negocio para que nunca quede por debajo de 0.
     */
    fun addKarma(cognitoId: String, amount: Int): UserProfileResponse {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Usuario no encontrado con ID de Cognito: $cognitoId") }

        val currentKarma = user.karmaBalance ?: 0
        val newKarma = currentKarma + amount

        // Regla de negocio: El karma no puede ser menor a cero
        user.karmaBalance = if (newKarma < 0) 0 else newKarma
        logger.info("Karma actualizado para $cognitoId. Ajuste: $amount | Nuevo saldo: ${user.karmaBalance}")
        return userRepository.save(user).toResponse()
    }
}