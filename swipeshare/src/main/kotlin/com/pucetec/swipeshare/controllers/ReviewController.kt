package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.dto.ReviewRequest
import com.pucetec.swipeshare.dto.ReviewResponse
import com.pucetec.swipeshare.services.ReviewService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
class ReviewController(
    val reviewService: ReviewService
) {
    private val logger = LoggerFactory.getLogger(ReviewController::class.java)

    @PostMapping("/api/reviews")
    fun createReview(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: ReviewRequest
    ): ReviewResponse {
        val cognitoId = jwt.subject!!
        logger.info("User $cognitoId is submitting a review")
        return reviewService.createReview(cognitoId, request)
    }

    @GetMapping("/api/reviews/target/{targetUserId}")
    fun getReviewsForUser(
        @PathVariable targetUserId: String
    ): List<ReviewResponse> {
        logger.info("Getting reviews for user id $targetUserId")
        return reviewService.getReviewsForUser(targetUserId)
    }
}