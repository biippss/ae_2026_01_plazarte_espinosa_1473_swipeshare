package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ReviewRequest
import com.pucetec.swipeshare.entities.Review
import com.pucetec.swipeshare.repositories.ReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
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

    private val cognitoId = "reviewer-sub"

    @BeforeEach
    fun setUp() {
        reviewService = ReviewService(reviewRepository, restTemplate)
    }

    @Test
    fun `createReview saves review and notifies user service when rating is 5`() {
        val request = ReviewRequest(targetUserId = "target-user", rating = 5, comment = "Excelente intercambio")
        val savedReview = Review(id = 1L, reviewerId = cognitoId, targetUserId = "target-user", rating = 5, comment = "Excelente intercambio")

        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(savedReview)

        val response = reviewService.createReview(cognitoId, request)

        assertEquals(1L, response.id)
        assertEquals(5, response.rating)
        val expectedUrl = "http://localhost:8081/api/internal/users/target-user/karma?amount=5"
        verify(restTemplate).put(eq(expectedUrl), eq(null))
    }

    @Test
    fun `getReviewsForUser returns reviews associated with user`() {
        val reviews = listOf(
            Review(id = 1L, reviewerId = "user1", targetUserId = "target-user", rating = 5, comment = "Bueno"),
            Review(id = 2L, reviewerId = "user2", targetUserId = "target-user", rating = 4, comment = "Puntual")
        )
        `when`(reviewRepository.findByTargetUserId("target-user")).thenReturn(reviews)

        val responses = reviewService.getReviewsForUser("target-user")

        assertEquals(2, responses.size)
        assertEquals("Bueno", responses[0].comment)
    }
    @Test
    fun `createReview handles rating 3 without notifying user service`() {
        val request = ReviewRequest(targetUserId = "target-user", rating = 3, comment = "Regular")
        val savedReview = Review(id = 2L, reviewerId = cognitoId, targetUserId = "target-user", rating = 3, comment = "Regular")

        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(savedReview)

        val response = reviewService.createReview(cognitoId, request)

        assertEquals(2L, response.id)
        assertEquals(3, response.rating)
    }
    @Test
    fun `createReview handles rating 1 and deducts karma`() {
        val request = ReviewRequest(targetUserId = "target-user", rating = 1, comment = "Mala experiencia")
        val savedReview = Review(id = 3L, reviewerId = cognitoId, targetUserId = "target-user", rating = 1, comment = "Mala experiencia")

        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(savedReview)

        val response = reviewService.createReview(cognitoId, request)

        assertEquals(3L, response.id)
        assertEquals(1, response.rating)
        val expectedUrl = "http://localhost:8081/api/internal/users/target-user/karma?amount=-5"
        verify(restTemplate).put(eq(expectedUrl), eq(null))
    }

    @Test
    fun `createReview handles restTemplate exception gracefully when user service is down`() {
        val request = ReviewRequest(targetUserId = "target-user", rating = 5, comment = "Excelente")
        val savedReview = Review(id = 4L, reviewerId = cognitoId, targetUserId = "target-user", rating = 5, comment = "Excelente")

        `when`(reviewRepository.save(any(Review::class.java))).thenReturn(savedReview)
        val expectedUrl = "http://localhost:8081/api/internal/users/target-user/karma?amount=5"
        `when`(restTemplate.put(eq(expectedUrl), eq(null))).thenThrow(RuntimeException("Connection refused"))

        val response = reviewService.createReview(cognitoId, request)

        assertEquals(4L, response.id)
        assertEquals(5, response.rating)
    }
}