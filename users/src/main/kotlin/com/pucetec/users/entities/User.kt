package com.pucetec.users.entities

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "cognito_id", nullable = false, unique = true)
    val cognitoId: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    var bio: String? = null,

    var phone: String? = null,

    @Column(name = "karma_balance", nullable = false)
    var karmaBalance: Int = 0
)