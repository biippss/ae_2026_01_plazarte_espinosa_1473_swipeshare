package com.pucetec.swipeshare.mappers

import com.pucetec.swipeshare.dto.UserRequest
import com.pucetec.swipeshare.dto.UserResponse
import com.pucetec.swipeshare.entities.User

// Mapea un request + su cognitoId a un entity.
// El cognitoId se pasa aparte porque no viaja en el request: sale del token.
fun UserRequest.toEntity(cognitoId: String) = User(
    cognitoId = cognitoId,
    name = this.name,
    email = this.email,
    phone = this.phone
    // karmaBalance inicia en 0 por defecto en la Entity
)

// Mapea un entity a un response, incluyendo su karmaBalance actual
fun User.toResponse() = UserResponse(
    id = this.id,
    cognitoId = this.cognitoId,
    name = this.name,
    email = this.email,
    phone = this.phone,
    karmaBalance = this.karmaBalance
)