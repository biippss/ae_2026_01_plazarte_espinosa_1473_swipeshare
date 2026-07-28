package com.pucetec.swipeshare.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "reviews")
class Review(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "reviewer_id", nullable = false)
    val reviewerId: Long = 0L,

    @Column(name = "target_user_id", nullable = false)
    val targetUserId: Long = 0L,

    @Column(nullable = false)
    val rating: Int = 0,

    val comment: String? = null
)