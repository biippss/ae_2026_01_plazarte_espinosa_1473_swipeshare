package com.pucetec.swipeshare.mappers

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchResponse
import com.pucetec.swipeshare.entities.Match

// El MatchRequest trae los IDs de los items, pero en la Entity guardamos a los usuarios.
// Por eso, tu capa Service debe buscar quiénes son los dueños de esos items y pasar sus IDs aquí.
fun MatchRequest.toEntity(user1Id: Long, user2Id: Long) = Match(
    user1Id = user1Id,
    user2Id = user2Id
    // El status se asume "PENDING" automáticamente por tu configuración por defecto en la Entity
)

fun Match.toResponse() = MatchResponse(
    id = this.id,
    user1Id = this.user1Id,
    user2Id = this.user2Id,
    status = this.status
)