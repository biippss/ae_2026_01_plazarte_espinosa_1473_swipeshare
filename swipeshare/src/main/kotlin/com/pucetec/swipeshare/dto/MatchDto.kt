package com.pucetec.swipeshare.dto

enum class MatchStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class MatchRequest(
    val offeredItemId: Long? = null,
    val requestedItemId: Long
)

data class UpdateMatchStatusDto(
    val status: MatchStatus
)

data class MatchResponse(
    val id: Long,
    val user1Id: String,
    val user2Id: String,
    val status: String,
    val offeredItemId: Long?,
    val requestedItemId: Long
)