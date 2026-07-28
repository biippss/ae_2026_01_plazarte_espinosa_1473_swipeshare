package com.pucetec.swipeshare.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    // El identificador del usuario en Cognito (claim "sub").
    @Column(unique = true, nullable = false)
    val cognitoId: String = "",

    // Cambiamos a 'var' para poder actualizar el perfil
    var name: String = "",

    var email: String? = null,

    var phone: String? = null,

    // Usamos 'var' en lugar de 'val' porque el karma cambiará constantemente
    // conforme los estudiantes reciban calificaciones en sus intercambios.
    @Column(nullable = false)
    var karmaBalance: Int = 0
)