package com.pucetec.swipeshare.mappers

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.dto.ItemResponse
import com.pucetec.swipeshare.entities.Item

// Mapea el request a Entity. El ownerId (ID interno del estudiante) lo inyecta el servicio.
fun ItemRequest.toEntity(ownerId: Long) = Item(
    title = this.title,
    description = this.description,
    category = this.category,
    imageUrl = this.imageUrl,
    ownerId = ownerId
)

// Mapea la Entity a Response para devolver al cliente
fun Item.toResponse() = ItemResponse(
    id = this.id,
    title = this.title,
    description = this.description,
    category = this.category,
    imageUrl = this.imageUrl,
    ownerId = this.ownerId
)