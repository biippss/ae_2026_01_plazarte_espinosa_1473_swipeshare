package com.pucetec.swipeshare.repositories

import com.pucetec.swipeshare.entities.Match
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchRepository : JpaRepository<Match, Long> {

    // Busca los intercambios donde el estudiante participe (sea el que ofrece o el que recibe)
    fun findByUser1IdOrUser2Id(user1Id: Long, user2Id: Long): List<Match>

    // Sirve para el endpoint público de stats (para contar matches activos)
    fun countByStatus(status: String): Long
}