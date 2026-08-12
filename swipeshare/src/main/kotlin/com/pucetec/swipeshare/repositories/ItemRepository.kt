package com.pucetec.swipeshare.repositories

import com.pucetec.swipeshare.entities.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long> {

    // Returns all items published by a specific user
    fun findByOwnerId(ownerId: String): List<Item>

    // Returns all items except those belonging to the specified owner
    fun findByOwnerIdNot(ownerId: String): List<Item>


}