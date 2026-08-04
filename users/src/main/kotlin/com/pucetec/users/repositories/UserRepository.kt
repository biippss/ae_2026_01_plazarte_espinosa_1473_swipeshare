package com.pucetec.users.repositories

import com.pucetec.users.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByCognitoId(cognitoId: String): Optional<User>
    fun existsByCognitoId(cognitoId: String): Boolean
}