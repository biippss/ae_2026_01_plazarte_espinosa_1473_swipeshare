package com.pucetec.swipeshare.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "items")
class Item(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    var title: String,

    var description: String,

    var category: String,

    var imageUrl: String? = null,

    // CAMBIO VITAL: Ahora guarda el ID de Cognito (UUID en texto)
    @Column(name = "owner_id", nullable = false)
    val ownerId: String
)