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

    // Crear una nueva reseña y actualizar el Karma del usuario objetivo
    fun createReview(cognitoId: String, request: ReviewRequest): ReviewResponse {
        logger.info("event=review.created | msg=Review submitted | targetUserId={} rating={}", request.targetUserId, request.rating)

        val review = request.toEntity(reviewerId = cognitoId)
        val savedReview = reviewRepository.save(review)

        // Calcular el ajuste de karma según la calificación
        val karmaDelta = when (request.rating) {
            4, 5 -> 5
            1, 2 -> -5
            else -> 0
        }

        // Notificar al microservicio 'users' para ajustar el karma
        if (karmaDelta != 0) {
            try {
                val url = "http://users:8081/api/users/internal/${request.targetUserId}/karma?amount=$karmaDelta"
                restTemplate.postForEntity(url, null, Void::class.java)
                logger.info("event=karma.updated | msg=Target user karma updated | targetUserId={} delta={}", request.targetUserId, karmaDelta)
            } catch (e: Exception) {
                logger.warn("event=karma.failed | msg=Could not update user karma | error=\"{}\"", e.message)
            }
        }

        return savedReview.toResponse()
    }

    // Obtener todas las reseñas dirigidas a un usuario específico
    fun getReviewsForUser(targetUserId: String): List<ReviewResponse> {
        return reviewRepository.findByTargetUserId(targetUserId).map { it.toResponse() }
    }
}