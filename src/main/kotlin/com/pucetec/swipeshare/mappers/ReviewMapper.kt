package com.pucetec.swipeshare.mappers

import com.pucetec.swipeshare.dto.ReviewRequest
import com.pucetec.swipeshare.dto.ReviewResponse
import com.pucetec.swipeshare.entities.Review

// El reviewerId es quien hace la reseña (sacado del token en el servicio).
fun ReviewRequest.toEntity(reviewerId: Long) = Review(
    reviewerId = reviewerId,
    targetUserId = this.targetUserId,
    rating = this.rating,
    comment = this.comment
)

fun Review.toResponse() = ReviewResponse(
    id = this.id,
    reviewerId = this.reviewerId,
    targetUserId = this.targetUserId,
    rating = this.rating,
    comment = this.comment
)