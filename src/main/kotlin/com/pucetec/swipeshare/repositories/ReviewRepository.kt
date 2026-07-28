package com.pucetec.swipeshare.repositories

import com.pucetec.swipeshare.entities.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {

    // Busca todas las reseñas que impactan a un usuario en especifico (targetUserId)
    fun findByTargetUserId(targetUserId: Long): List<Review>
}