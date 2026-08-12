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

    private val cognitoId = "user-cognito-123"

    // --- getMyProfile ---
    @Test
    fun `getMyProfile returns profile when user exists`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = "Bio", phone = "+593", karmaBalance = 10)
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))

        val response = userService.getMyProfile(cognitoId)

        assertEquals("Israel", response.name)
        assertEquals("israel@puce.edu.ec", response.email)
    }

    @Test
    fun `getMyProfile throws UserNotFoundException when user missing`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.getMyProfile(cognitoId)
        }
    }

    // --- saveOrUpdateProfile ---
    @Test
    fun `saveOrUpdateProfile throws BlankNameException when name is blank`() {
        val request = UserProfileRequest(name = "   ", email = "israel@puce.edu.ec", bio = null, phone = null)

        assertThrows<BlankNameException> {
            userService.saveOrUpdateProfile(cognitoId, "israel@puce.edu.ec", true, request)
        }
    }

    @Test
    fun `saveOrUpdateProfile throws IllegalArgumentException when email is not institutional`() {
        val request = UserProfileRequest(name = "Israel", email = "israel@gmail.com", bio = null, phone = null)

        assertThrows<IllegalArgumentException> {
            userService.saveOrUpdateProfile(cognitoId, "israel@gmail.com", true, request)
        }
    }

    @Test
    fun `saveOrUpdateProfile throws IllegalArgumentException when email is not verified`() {
        val request = UserProfileRequest(name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null)

        assertThrows<IllegalArgumentException> {
            userService.saveOrUpdateProfile(cognitoId, "israel@puce.edu.ec", false, request)
        }
    }

    @Test
    fun `saveOrUpdateProfile updates existing user when found`() {
        val request = UserProfileRequest(name = "Israel Updated", email = "israel@puce.edu.ec", bio = "New Bio", phone = "123")
        val existingUser = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = "Old Bio", phone = null, karmaBalance = 5)
        val savedUser = User(id = 1L, cognitoId = cognitoId, name = "Israel Updated", email = "israel@puce.edu.ec", bio = "New Bio", phone = "123", karmaBalance = 5)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(existingUser))
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        val response = userService.saveOrUpdateProfile(cognitoId, "israel@puce.edu.ec", true, request)

        assertEquals("Israel Updated", response.name)
        assertEquals("New Bio", response.bio)
    }

    @Test
    fun `saveOrUpdateProfile creates new user when not found`() {
        val request = UserProfileRequest(name = "New User", email = "new@puce.edu.ec", bio = "Bio", phone = "123")
        val savedUser = User(id = 2L, cognitoId = cognitoId, name = "New User", email = "new@puce.edu.ec", bio = "Bio", phone = "123", karmaBalance = 0)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        val response = userService.saveOrUpdateProfile(cognitoId, "new@puce.edu.ec", true, request)

        assertEquals("New User", response.name)
        assertEquals(0, response.karmaBalance)
    }

    // --- updateProfile ---
    @Test
    fun `updateProfile throws BlankNameException when name blank`() {
        val request = UserProfileRequest(name = "", email = "israel@puce.edu.ec", bio = null, phone = null)

        assertThrows<BlankNameException> {
            userService.updateProfile(cognitoId, request)
        }
    }

    @Test
    fun `updateProfile throws UserNotFoundException when user missing`() {
        val request = UserProfileRequest(name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null)
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.updateProfile(cognitoId, request)
        }
    }

    @Test
    fun `updateProfile updates and saves user`() {
        val request = UserProfileRequest(name = "Israel Mod", email = "israel@puce.edu.ec", bio = "New Bio", phone = "+593")
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 10)
        val updatedUser = User(id = 1L, cognitoId = cognitoId, name = "Israel Mod", email = "israel@puce.edu.ec", bio = "New Bio", phone = "+593", karmaBalance = 10)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any(User::class.java))).thenReturn(updatedUser)

        val response = userService.updateProfile(cognitoId, request)

        assertEquals("Israel Mod", response.name)
        assertEquals("New Bio", response.bio)
    }

    // --- deleteProfile ---
    @Test
    fun `deleteProfile deletes user when found`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 0)
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))

        userService.deleteProfile(cognitoId)

        verify(userRepository).delete(user)
    }

    @Test
    fun `deleteProfile throws UserNotFoundException when missing`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.deleteProfile(cognitoId)
        }
    }

    // --- getProfileByCognitoId ---
    @Test
    fun `getProfileByCognitoId returns public profile`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 5)
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))

        val response = userService.getProfileByCognitoId(cognitoId)

        assertEquals(1L, response.id)
        assertEquals("Israel", response.name)
    }

    @Test
    fun `getProfileByCognitoId throws UserNotFoundException when missing`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.getProfileByCognitoId(cognitoId)
        }
    }

    // --- addKarma ---
    @Test
    fun `addKarma increases karma balance`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 10)
        val updatedUser = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 15)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any(User::class.java))).thenReturn(updatedUser)

        val response = userService.addKarma(cognitoId, 5)

        assertEquals(15, response.karmaBalance)
    }

    @Test
    fun `addKarma clamps karma to zero when result is negative`() {
        val user = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 2)
        val updatedUser = User(id = 1L, cognitoId = cognitoId, name = "Israel", email = "israel@puce.edu.ec", bio = null, phone = null, karmaBalance = 0)

        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.of(user))
        `when`(userRepository.save(any(User::class.java))).thenReturn(updatedUser)

        val response = userService.addKarma(cognitoId, -5)

        assertEquals(0, response.karmaBalance)
    }

    @Test
    fun `addKarma throws UserNotFoundException when user missing`() {
        `when`(userRepository.findByCognitoId(cognitoId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.addKarma(cognitoId, 5)
        }
    }
}