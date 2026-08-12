package com.pucetec.users.services

import com.pucetec.users.dto.UserProfileRequest
import com.pucetec.users.dto.UserProfileResponse
import com.pucetec.users.exceptions.BlankNameException
import com.pucetec.users.exceptions.UserNotFoundException
import com.pucetec.users.mappers.toEntity
import com.pucetec.users.mappers.toResponse
import com.pucetec.users.repositories.UserRepository
import com.pucetec.users.utils.LogUtils.maskEmail
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // Obtener mi propio perfil
    fun getMyProfile(cognitoId: String): UserProfileResponse {
        return userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("User not found with ID: $cognitoId") }
            .toResponse()
    }

    // Guardar o actualizar mi perfil con validaciones institucionales
    fun saveOrUpdateProfile(
        cognitoId: String,
        emailFromJwt: String,
        isEmailVerified: Boolean,
        request: UserProfileRequest
    ): UserProfileResponse {
        if (request.name.isBlank()) {
            throw BlankNameException("Name cannot be blank")
        }

        val cleanEmail = emailFromJwt.trim().lowercase()
        if (!cleanEmail.endsWith("@puce.edu.ec")) {
            throw IllegalArgumentException("Access denied: Institutional PUCE email required (@puce.edu.ec)")
        }

        if (!isEmailVerified) {
            throw IllegalArgumentException("Access denied: Email must be verified in AWS Cognito")
        }

        val existingUser = userRepository.findByCognitoId(cognitoId)
        val response = if (existingUser.isPresent) {
            val user = existingUser.get()
            user.name = request.name
            user.email = cleanEmail
            user.bio = request.bio
            user.phone = request.phone
            userRepository.save(user).toResponse()
        } else {
            val newUser = request.toEntity(cognitoId = cognitoId)
            newUser.email = cleanEmail
            userRepository.save(newUser).toResponse()
        }

        logger.info("event=user.profile_saved | msg=User profile saved | email={}", maskEmail(cleanEmail))
        return response
    }

    // Actualizar información del perfil
    fun updateProfile(cognitoId: String, request: UserProfileRequest): UserProfileResponse {
        if (request.name.isBlank()) {
            throw BlankNameException("Name cannot be blank")
        }

        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("User not found with ID: $cognitoId") }

        user.name = request.name
        user.bio = request.bio
        user.phone = request.phone

        val updatedUser = userRepository.save(user)
        logger.info("event=user.profile_updated | msg=Profile updated successfully")
        return updatedUser.toResponse()
    }

    // Eliminar mi perfil
    fun deleteProfile(cognitoId: String) {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("User not found with ID: $cognitoId") }

        userRepository.delete(user)
        logger.info("event=user.deleted | msg=User profile deleted")
    }

    // Obtener perfil público de otro usuario
    fun getProfileByCognitoId(cognitoId: String): UserProfileResponse {
        return userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("User not found with ID: $cognitoId") }
            .toResponse()
    }

    // Ajustar saldo de Karma de un usuario
    fun addKarma(cognitoId: String, amount: Int): UserProfileResponse {
        val user = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("User not found with ID: $cognitoId") }

        val currentKarma = user.karmaBalance
        val newKarma = currentKarma + amount

        user.karmaBalance = if (newKarma < 0) 0 else newKarma
        val updatedUser = userRepository.save(user)
        logger.info("event=user.karma_updated | msg=Karma balance adjusted | delta={} newBalance={}", amount, updatedUser.karmaBalance)
        return updatedUser.toResponse()
    }
}