package com.pucetec.swipeshare.repositories

import com.pucetec.swipeshare.entities.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long> {

    // Devuelve todos los objetos publicados por un estudiante especifico
    fun findByOwnerId(ownerId: String): List<Item>
}