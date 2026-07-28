package com.pucetec.swipeshare.dto

/***
 * Lo que envia el cliente al calificar a otro estudiante despues de un intercambio.
 * Esta calificación impactará en el karmaBalance del targetUserId.
 * {targetUserId: 3, rating: 5, comment: "Excelente intercambio, muy puntual"}
 */
data class ReviewRequest(
    val targetUserId: Long,
    val rating: Int,
    val comment: String?
)

/***
 * Lo que devuelve el micro: la reseña registrada en el sistema.
 * {id: 7, reviewerId: 1, targetUserId: 3, rating: 5, comment: "Excelente intercambio, muy puntual"}
 */
data class ReviewResponse(
    val id: Long,
    val reviewerId: Long,
    val targetUserId: Long,
    val rating: Int,
    val comment: String?
)