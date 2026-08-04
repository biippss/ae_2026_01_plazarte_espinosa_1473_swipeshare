package com.pucetec.users.dto

data class UserProfileRequest(
    val name: String,
    val email: String,
    val bio: String? = null,
    val phone: String? = null
)

data class UserProfileResponse(
    val id: Long,
    val cognitoId: String,
    val name: String,
    val email: String,
    val bio: String?,
    val phone: String?,
    val karmaBalance: Int
)