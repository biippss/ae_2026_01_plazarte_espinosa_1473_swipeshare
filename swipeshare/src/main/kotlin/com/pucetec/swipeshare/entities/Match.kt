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

    // Cambiado de Long a String para guardar el Cognito ID
    @Column(name = "user1_id", nullable = false)
    val user1Id: String = "",

    @Column(name = "user2_id", nullable = false)
    val user2Id: String = "",

    @Column(nullable = false)
    var status: String = "PENDING"
)