package com.pucetec.swipeshare.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "matches")
class Match(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "user1_id", nullable = false)
    val user1Id: String = "",

    @Column(name = "user2_id", nullable = false)
    val user2Id: String = "",

    @Column(nullable = false)
    var status: String = "PENDING",

    @Column(name = "offered_item_id")
    val offeredItemId: Long? = null,

    @Column(name = "requested_item_id", nullable = false)
    val requestedItemId: Long = 0L
)