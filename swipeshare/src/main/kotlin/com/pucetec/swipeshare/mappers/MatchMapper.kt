package com.pucetec.swipeshare.mappers

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchResponse
import com.pucetec.swipeshare.dto.SwipeResponse
import com.pucetec.swipeshare.entities.Match

fun MatchRequest.toEntity(user1Id: String, user2Id: String) = Match(
    user1Id = user1Id,
    user2Id = user2Id,
    offeredItemId = this.offeredItemId,
    requestedItemId = this.requestedItemId
)

fun Match.toResponse() = MatchResponse(
    id = this.id,
    user1Id = this.user1Id,
    user2Id = this.user2Id,
    status = this.status,
    offeredItemId = this.offeredItemId,
    requestedItemId = this.requestedItemId
)

fun Match.toSwipeResponse(message: String = "Match created successfully") = SwipeResponse(
    isMatch = true,
    matchId = this.id,
    message = message
)