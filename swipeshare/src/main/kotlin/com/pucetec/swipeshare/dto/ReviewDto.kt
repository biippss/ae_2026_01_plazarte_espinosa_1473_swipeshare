package com.pucetec.swipeshare.dto

/***
 * Lo que envía el cliente al calificar a otro estudiante después de un intercambio.
 * {targetUserId: "3f2a1b94-8c7d-...", rating: 5, comment: "Excelente intercambio, muy puntual"}
 */
data class ReviewRequest(
    val targetUserId: String,
    val rating: Int,
    val comment: String?
)

/***
 * Lo que devuelve el micro: la reseña registrada en el sistema.
 * {id: 7, reviewerId: "1a2b...", targetUserId: "3f2a...", rating: 5, comment: "Excelente"}
 */
data class ReviewResponse(
    val id: Long,
    val reviewerId: String,
    val targetUserId: String,
    val rating: Int,
    val comment: String?
)