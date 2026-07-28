package com.pucetec.swipeshare.dto

/***
 * Lo que envia el cliente al intentar hacer match (solicitar un intercambio).
 * {offeredItemId: 10, requestedItemId: 25}
 */
data class MatchRequest(
    val offeredItemId: Long,
    val requestedItemId: Long
)

/***
 * Lo que devuelve el micro: el estado de la solicitud de intercambio entre los dos estudiantes.
 * {id: 5, user1Id: 1, user2Id: 3, status: "PENDING"}
 */
data class MatchResponse(
    val id: Long,
    val user1Id: Long,
    val user2Id: Long,
    val status: String
)