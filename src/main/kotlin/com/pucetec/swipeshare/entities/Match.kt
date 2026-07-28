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
    val user1Id: Long = 0L,

    @Column(name = "user2_id", nullable = false)
    val user2Id: Long = 0L,

    // Status por defecto: PENDING, ACCEPTED, o REJECTED.
    // Usamos 'var' porque el estado se actualizará cuando la otra persona acepte.
    @Column(nullable = false)
    var status: String = "PENDING"
)