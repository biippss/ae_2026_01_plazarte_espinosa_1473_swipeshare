package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ReviewRequest
import com.pucetec.swipeshare.dto.ReviewResponse
import com.pucetec.swipeshare.mappers.toEntity
import com.pucetec.swipeshare.mappers.toResponse
import com.pucetec.swipeshare.repositories.ReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val restTemplate: RestTemplate = RestTemplate()
) {

    private val logger = LoggerFactory.getLogger(ReviewService::class.java)

    fun createReview(cognitoId: String, request: ReviewRequest): ReviewResponse {
        logger.info("Estudiante $cognitoId está dejando ${request.rating} estrellas al usuario ${request.targetUserId}")

        // 1. Guardar la reseña en la base de datos de swipe-share
        val review = request.toEntity(reviewerId = cognitoId)
        val savedReview = reviewRepository.save(review)

        // 2. Calcular el ajuste de karma según las estrellas
        val karmaDelta = when (request.rating) {
            4, 5 -> 5   // Buena reseña: +5 puntos
            1, 2 -> -5  // Mala reseña: -5 puntos
            else -> 0   // Reseña neutral (3 estrellas): 0 puntos
        }

        // 3. Notificar automáticamente al microservicio 'users' (Puerto 8081)
        if (karmaDelta != 0) {
            try {
                val url = "http://localhost:8081/api/internal/users/${request.targetUserId}/karma?amount=$karmaDelta"
                restTemplate.put(url, null)
                logger.info("Karma de ${request.targetUserId} actualizado exitosamente ($karmaDelta puntos)")
            } catch (e: Exception) {
                // Si el servicio de usuarios está apagado, logueamos el error pero no fallamos la creación de la reseña
                logger.error("No se pudo conectar con el microservicio de usuarios para actualizar el karma: ${e.message}")
            }
        }

        return savedReview.toResponse()
    }

    fun getReviewsForUser(targetUserId: String): List<ReviewResponse> {
        return reviewRepository.findByTargetUserId(targetUserId).map { it.toResponse() }
    }
}