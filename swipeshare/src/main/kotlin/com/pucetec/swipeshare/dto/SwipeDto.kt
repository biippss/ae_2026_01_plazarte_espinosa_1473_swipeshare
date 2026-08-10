package com.pucetec.swipeshare.dto

enum class SwipeType {
    LIKE,
    DISLIKE
}

data class SwipeRequest(
    val targetItemId: Long,
    val type: SwipeType,
    val offeredItemId: Long? = null
)

data class SwipeResponse(
    val isMatch: Boolean,
    val matchId: Long? = null,
    val message: String
)