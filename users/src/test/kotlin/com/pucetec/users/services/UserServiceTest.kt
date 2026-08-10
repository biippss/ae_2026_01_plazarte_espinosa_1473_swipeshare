package com.pucetec.users.services

import com.pucetec.users.dto.UserProfileRequest
import com.pucetec.users.entities.User
import com.pucetec.users.exceptions.BlankNameException
import com.pucetec.users.exceptions.UserNotFoundException
import com.pucetec.users.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    private val cognitoId = "sub-cognito-user-123"

    @Test
    fun `getMyProfile returns profile when user exists`() {
        val user = User(
            id = 1L,
            cognitoId = cognitoId,
            name = "Israel Plazarte",
            email = "israel@puce.edu.ec",
            bio = "Software student",
            phone = "0999999999",
            karmaBalance = 10
        )
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))

        val response = userService.getMyProfile(cognitoId)

        assertEquals(1L, response.id)
        assertEquals(cognitoId, response.cognitoId)
        assertEquals("Israel Plazarte", response.name)
    }

    @Test
    fun `getMyProfile throws UserNotFoundException when user does not exist`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.getMyProfile(cognitoId)
        }
    }

    @Test
    fun `saveOrUpdateProfile throws BlankNameException when name is blank`() {
        val request = UserProfileRequest(name = "   ", email = "test@puce.edu.ec", bio = null, phone = null)

        assertThrows<BlankNameException> {
            userService.saveOrUpdateProfile(cognitoId, request)
        }
    }

    @Test
    fun `saveOrUpdateProfile updates existing profile when user is present`() {
        val request = UserProfileRequest(name = "Updated Name", email = "new@puce.edu.ec", bio = "New Bio", phone = "0988888888")
        val existingUser = User(id = 1L, cognitoId = cognitoId, name = "Old Name", email = "old@puce.edu.ec", bio = "Old Bio", phone = "0999999999", karmaBalance = 5)
        val updatedUser = User(id = 1L, cognitoId = cognitoId, name = "Updated Name", email = "new@puce.edu.ec", bio = "New Bio", phone = "0988888888", karmaBalance = 5)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(existingUser))
        `when`(userRepository.save(any(User::class.java))).thenReturn(updatedUser)

        val response = userService.saveOrUpdateProfile(cognitoId, request)

        assertEquals("Updated Name", response.name)
        assertEquals("new@puce.edu.ec", response.email)
    }

    @Test
    fun `saveOrUpdateProfile creates new profile when user is not present`() {
        val request = UserProfileRequest(name = "New User", email = "newuser@puce.edu.ec", bio = "Bio", phone = null)
        val newUser = User(id = 2L, cognitoId = cognitoId, name = "New User", email = "newuser@puce.edu.ec", bio = "Bio", phone = null, karmaBalance = 0)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())
        `when`(userRepository.save(any(User::class.java))).thenReturn(newUser)

        val response = userService.saveOrUpdateProfile(cognitoId, request)

        assertEquals(2L, response.id)
        assertEquals("New User", response.name)
    }

    @Test
    fun `updateProfile updates profile when user exists`() {
        val request = UserProfileRequest(name = "Israel Modified", email = "mod@puce.edu.ec", bio = "Updated", phone = "0977777777")
        val existingUser = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = "Old", phone = "0999999999", karmaBalance = 15)
        val savedUser = User(id = 1L, cognitoId = cognitoId, name = "Israel Modified", email = "mod@puce.edu.ec", bio = "Updated", phone = "0977777777", karmaBalance = 15)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(existingUser))
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        val response = userService.updateProfile(cognitoId, request)

        assertEquals("Israel Modified", response.name)
        assertEquals("mod@puce.edu.ec", response.email)
    }

    @Test
    fun `updateProfile throws BlankNameException when name is blank`() {
        val request = UserProfileRequest(name = "", email = "test@puce.edu.ec", bio = null, phone = null)

        assertThrows<BlankNameException> {
            userService.updateProfile(cognitoId, request)
        }
    }

    @Test
    fun `updateProfile throws UserNotFoundException when user does not exist`() {
        val request = UserProfileRequest(name = "Valid Name", email = "test@puce.edu.ec", bio = null, phone = null)
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.updateProfile(cognitoId, request)
        }
    }

    @Test
    fun `deleteProfile deletes user when user exists`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 0)
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))

        userService.deleteProfile(cognitoId)

        verify(userRepository).delete(user)
    }

    @Test
    fun `deleteProfile throws UserNotFoundException when user does not exist`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.deleteProfile(cognitoId)
        }
    }

    @Test
    fun `getProfileByCognitoId returns profile when user exists`() {
        val user = User(id = 3L, cognitoId = cognitoId, name = "Target User", email = "target@puce.edu.ec", bio = null, phone = null, karmaBalance = 20)
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))

        val response = userService.getProfileByCognitoId(cognitoId)

        assertEquals(3L, response.id)
        assertEquals("Target User", response.name)
    }

    @Test
    fun `getProfileByCognitoId throws UserNotFoundException when user does not exist`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.getProfileByCognitoId(cognitoId)
        }
    }

    @Test
    fun `addKarma increases karma balance correctly`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 10)
        val updatedUser = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 15)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any(User::class.java))).thenReturn(updatedUser)

        val response = userService.addKarma(cognitoId, 5)

        assertEquals(15, response.karmaBalance)
    }

    @Test
    fun `addKarma floors karma balance to zero when result is negative`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 2)
        val updatedUser = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 0)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any(User::class.java))).thenReturn(updatedUser)

        val response = userService.addKarma(cognitoId, -5)

        assertEquals(0, response.karmaBalance)
    }

    @Test
    fun `addKarma throws UserNotFoundException when user does not exist`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.addKarma(cognitoId, 5)
        }
    }
}