package com.pucetec.swipeshare.mappers

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.dto.ItemResponse
import com.pucetec.swipeshare.entities.Item

// Ahora ownerId recibe un String (Cognito ID)
fun ItemRequest.toEntity(ownerId: String) = Item(
    title = this.title,
    description = this.description,
    category = this.category,
    imageUrl = this.imageUrl,
    ownerId = ownerId
)

fun Item.toResponse() = ItemResponse(
    id = this.id,
    title = this.title,
    description = this.description,
    category = this.category,
    imageUrl = this.imageUrl,
    ownerId = this.ownerId
)