package com.pucetec.swipeshare.dto

data class MatchRequest(
    val offeredItemId: Long,
    val requestedItemId: Long
)

data class MatchResponse(
    val id: Long,
    val user1Id: String,
    val user2Id: String,
    val status: String
)