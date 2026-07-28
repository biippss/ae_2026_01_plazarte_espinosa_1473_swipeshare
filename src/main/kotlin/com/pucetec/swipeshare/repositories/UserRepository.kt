package com.pucetec.swipeshare.repositories

import com.pucetec.swipeshare.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    // Spring Data genera la consulta a partir del nombre del metodo:
    // "buscar un User cuyo cognitoId sea igual al parametro".
    fun findByCognitoId(cognitoId: String): Optional<User>

    fun existsByCognitoId(cognitoId: String): Boolean
}