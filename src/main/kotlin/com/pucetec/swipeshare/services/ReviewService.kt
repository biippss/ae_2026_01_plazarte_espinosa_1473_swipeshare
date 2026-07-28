package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ReviewRequest
import com.pucetec.swipeshare.dto.ReviewResponse
import com.pucetec.swipeshare.exceptions.UserNotFoundException
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.ReviewRepository
import com.pucetec.swipeshare.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(ReviewService::class.java)

    fun createReview(cognitoId: String, request: ReviewRequest): ReviewResponse {
        val reviewer = userRepository.findByCognitoId(cognitoId)
            .orElseThrow { UserNotFoundException("Calificador no encontrado") }

        val targetUser = userRepository.findById(request.targetUserId)
            .orElseThrow { UserNotFoundException("El estudiante al que intentas calificar no existe") }

        logger.info("Estudiante ${reviewer.id} está dejando ${request.rating} estrellas a estudiante ${targetUser.id}")

        // REGLA DE NEGOCIO: Impacto directo en el karma del usuario objetivo
        // Si la calificación es alta (ej. 4 o 5) sube su balance, si es baja lo penaliza.
        val puntosKarma = request.rating - 3 // Regla simple: 5 estrellas = +2 karma, 1 estrella = -2 karma
        targetUser.karmaBalance += puntosKarma
        userRepository.save(targetUser)

        val review = request.toEntity(reviewer.id)
        return reviewRepository.save(review).toResponse()
    }

    fun getReviewsForUser(targetUserId: Long): List<ReviewResponse> {
        return reviewRepository.findByTargetUserId(targetUserId).map { it.toResponse() }
    }
}