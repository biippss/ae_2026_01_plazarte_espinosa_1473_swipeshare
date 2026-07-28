package com.pucetec.swipeshare.dto

/***
 * Lo que envia el cliente al registrar/actualizar su perfil.
 * El cognitoId NO viene aqui: se toma del token (claim "sub") en el endpoint /me,
 * o del path en los endpoints administrativos.
 * {name: "israel", email: "israel@puce.edu.ec", phone: "0999999999"}
 */
data class UserRequest(
    val name: String,
    val email: String?,
    val phone: String?
)

/***
 * Lo que devuelve el micro: el perfil ya asociado a su cognitoId, incluyendo su Karma actual.
 * {id: 1, cognitoId: "a1b2-...", name: "israel", email: "israel@puce.edu.ec", phone: "0999999999", karmaBalance: 100}
 */
data class UserResponse(
    val id: Long,
    val cognitoId: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val karmaBalance: Int
)