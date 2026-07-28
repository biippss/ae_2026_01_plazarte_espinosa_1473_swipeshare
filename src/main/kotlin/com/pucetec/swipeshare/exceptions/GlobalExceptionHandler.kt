package com.pucetec.swipeshare.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ItemNotFoundException::class)
    fun handleItemNotFoundException(
        e: ItemNotFoundException
    ): ResponseEntity<ExceptionResponse> {
        val response = ExceptionResponse(
            message = e.message ?: "Item no encontrado - ERROR",
            source = "ItemService"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response)
    }

    @ExceptionHandler(MatchNotFoundException::class)
    fun handleMatchNotFoundException(
        e: MatchNotFoundException
    ): ResponseEntity<ExceptionResponse> {
        val response = ExceptionResponse(
            message = e.message ?: "Match no encontrado - ERROR",
            source = "MatchService"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response)
    }

    @ExceptionHandler(ReviewNotFoundException::class)
    fun handleReviewNotFoundException(
        e: ReviewNotFoundException
    ): ResponseEntity<ExceptionResponse> {
        val response = ExceptionResponse(
            message = e.message ?: "Reseña no encontrada - ERROR",
            source = "ReviewService"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        e: UserNotFoundException
    ): ResponseEntity<ExceptionResponse> {
        val response = ExceptionResponse(
            message = e.message ?: "Usuario no encontrado - ERROR",
            source = "UserService"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response)
    }
}

data class ExceptionResponse(
    val message: String,
    val source: String,
)