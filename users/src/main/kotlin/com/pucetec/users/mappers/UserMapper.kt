package com.pucetec.users.mappers

import com.pucetec.users.dto.UserProfileRequest
import com.pucetec.users.dto.UserProfileResponse
import com.pucetec.users.entities.User

fun UserProfileRequest.toEntity(cognitoId: String) = User(
    cognitoId = cognitoId,
    name = this.name,
    email = this.email,
    bio = this.bio,
    phone = this.phone,
    karmaBalance = 0
)

fun User.toResponse() = UserProfileResponse(
    id = this.id,
    cognitoId = this.cognitoId,
    name = this.name,
    email = this.email,
    bio = this.bio,
    phone = this.phone,
    karmaBalance = this.karmaBalance
)