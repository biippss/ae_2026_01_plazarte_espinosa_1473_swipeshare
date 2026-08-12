package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ReviewRequest
import com.pucetec.swipeshare.entities.Review
import com.pucetec.swipeshare.repositories.ReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {

    @Mock
    private lateinit var reviewRepository: ReviewRepository

    @Mock
    private lateinit var restTemplate: RestTemplate

    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        // Instanciación explícita para evitar fallos de reflexión con @InjectMocks en Kotlin
        reviewService = ReviewService(reviewRepository, restTemplate)
    }

    @Test
    fun `createReview saves review with positive rating`() {
        val request = ReviewRequest(targetUserId = "user-target", rating = 5, comment = "Excelente")
        val savedReview = Review(id = 1L, reviewerId = "user-src", targetUserId = "user-target", rating = 5, comment = "Excelente")

        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(savedReview)

        val response = reviewService.createReview("user-src", request)

        assertEquals(1L, response.id)
        assertEquals(5, response.rating)
        assertEquals("user-target", response.targetUserId)
    }

    @Test
    fun `createReview saves review with negative rating`() {
        val request = ReviewRequest(targetUserId = "user-target", rating = 1, comment = "Malo")
        val savedReview = Review(id = 2L, reviewerId = "user-src", targetUserId = "user-target", rating = 1, comment = "Malo")

        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(savedReview)

        val response = reviewService.createReview("user-src", request)

        assertEquals(1, response.rating)
    }

    @Test
    fun `createReview saves review with neutral rating`() {
        val request = ReviewRequest(targetUserId = "user-target", rating = 3, comment = "Normal")
        val savedReview = Review(id = 3L, reviewerId = "user-src", targetUserId = "user-target", rating = 3, comment = "Normal")

        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(savedReview)

        val response = reviewService.createReview("user-src", request)

        assertEquals(3, response.rating)
    }

    @Test
    fun `getReviewsForUser returns review list for specified user`() {
        val reviews = listOf(
            Review(id = 1L, reviewerId = "user-src", targetUserId = "user-target", rating = 5, comment = "Super")
        )
        `when`(reviewRepository.findByTargetUserId("user-target")).thenReturn(reviews)

        val responseList = reviewService.getReviewsForUser("user-target")

        assertEquals(1, responseList.size)
        assertEquals("Super", responseList[0].comment)
    }
}