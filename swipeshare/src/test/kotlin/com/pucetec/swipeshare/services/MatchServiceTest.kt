package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.MatchRequest
import com.pucetec.swipeshare.dto.MatchStatus
import com.pucetec.swipeshare.dto.SwipeRequest
import com.pucetec.swipeshare.dto.SwipeType
import com.pucetec.swipeshare.dto.UpdateMatchStatusDto
import com.pucetec.swipeshare.entities.Item
import com.pucetec.swipeshare.entities.Match
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.MatchNotFoundException
import com.pucetec.swipeshare.exceptions.UnauthorizedItemAccessException
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MatchServiceTest {

    @Mock
    private lateinit var matchRepository: MatchRepository

    @Mock
    private lateinit var itemRepository: ItemRepository

    @InjectMocks
    private lateinit var matchService: MatchService

    private val cognitoId = "user-123"

    @Test
    fun `processSwipe processes DISLIKE without creating match`() {
        val targetItem = Item(id = 2L, title = "Silla", description = "Gamer", category = "MUEBLES", ownerId = "other-user")
        val swipeRequest = SwipeRequest(targetItemId = 2L, type = SwipeType.DISLIKE)

        `when`(itemRepository.findById(2L)).thenReturn(Optional.of(targetItem))

        val response = matchService.processSwipe(cognitoId, swipeRequest)

        assertFalse(response.isMatch)
        assertEquals("Item rejected successfully", response.message)
    }

    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when swiping on own item`() {
        val myItem = Item(id = 1L, title = "Mesa", description = "Madera", category = "MUEBLES", ownerId = cognitoId)
        val swipeRequest = SwipeRequest(targetItemId = 1L, type = SwipeType.LIKE)

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(myItem))

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(cognitoId, swipeRequest)
        }
    }

    @Test
    fun `processSwipe creates successful match when LIKE and user has items`() {
        val targetItem = Item(id = 2L, title = "Monitor", description = "24 pulg", category = "ELECTRONICA", ownerId = "other-user")
        val userItem = Item(id = 10L, title = "Teclado", description = "USB", category = "ELECTRONICA", ownerId = cognitoId)
        val swipeRequest = SwipeRequest(targetItemId = 2L, type = SwipeType.LIKE, offeredItemId = 10L)
        val savedMatch = Match(id = 100L, user1Id = cognitoId, user2Id = "other-user", status = "PENDING")

        `when`(itemRepository.findById(2L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(cognitoId)).thenReturn(listOf(userItem))
        `when`(matchRepository.save(any(Match::class.java))).thenReturn(savedMatch)

        val response = matchService.processSwipe(cognitoId, swipeRequest)

        assertTrue(response.isMatch)
        assertEquals(100L, response.matchId)
    }

    @Test
    fun `updateMatchStatus updates match status when user is participant`() {
        val existingMatch = Match(id = 1L, user1Id = cognitoId, user2Id = "other-user", status = "PENDING")
        val updateDto = UpdateMatchStatusDto(status = MatchStatus.APPROVED)
        val updatedMatch = Match(id = 1L, user1Id = cognitoId, user2Id = "other-user", status = "APPROVED")

        `when`(matchRepository.findById(1L)).thenReturn(Optional.of(existingMatch))
        `when`(matchRepository.save(any(Match::class.java))).thenReturn(updatedMatch)

        val response = matchService.updateMatchStatus(1L, cognitoId, updateDto)

        assertEquals("APPROVED", response.status)
    }

    @Test
    fun `updateMatchStatus throws UnauthorizedItemAccessException when user is not participant`() {
        val existingMatch = Match(id = 1L, user1Id = "user-A", user2Id = "user-B", status = "PENDING")
        val updateDto = UpdateMatchStatusDto(status = MatchStatus.APPROVED)

        `when`(matchRepository.findById(1L)).thenReturn(Optional.of(existingMatch))

        assertThrows<UnauthorizedItemAccessException> {
            matchService.updateMatchStatus(1L, cognitoId, updateDto)
        }
    }
    @Test
    fun `createMatch creates and returns match successfully`() {
        val requestedItem = Item(id = 2L, title = "Consola", description = "PS5", category = "JUEGOS", imageUrl = "https://example.com/ps5.jpg", ownerId = "target-owner")
        val matchRequest = MatchRequest(offeredItemId = 1L, requestedItemId = 2L)
        val savedMatch = Match(id = 50L, user1Id = cognitoId, user2Id = "target-owner", status = "PENDING")

        `when`(itemRepository.findById(2L)).thenReturn(Optional.of(requestedItem))
        `when`(matchRepository.save(any(Match::class.java))).thenReturn(savedMatch)

        val response = matchService.createMatch(cognitoId, matchRequest)

        assertEquals(50L, response.id)
        assertEquals("PENDING", response.status)
    }

    @Test
    fun `getMatchesByUser returns matches for participant`() {
        val matches = listOf(
            Match(id = 1L, user1Id = cognitoId, user2Id = "user-2", status = "APPROVED")
        )
        `when`(matchRepository.findByUser1IdOrUser2Id(cognitoId, cognitoId)).thenReturn(matches)

        val responses = matchService.getMatchesByUser(cognitoId)

        assertEquals(1, responses.size)
        assertEquals("APPROVED", responses[0].status)
    }
    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when user has no items`() {
        val targetItem = Item(id = 2L, title = "Silla", description = "Gamer", category = "MUEBLES", imageUrl = "https://example.com/silla.jpg", ownerId = "other-user")
        val swipeRequest = SwipeRequest(targetItemId = 2L, type = SwipeType.LIKE)

        `when`(itemRepository.findById(2L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(cognitoId)).thenReturn(emptyList())

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(cognitoId, swipeRequest)
        }
    }

    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when offered item does not belong to user`() {
        val targetItem = Item(id = 2L, title = "Silla", description = "Gamer", category = "MUEBLES", imageUrl = "https://example.com/silla.jpg", ownerId = "other-user")
        val userItem = Item(id = 10L, title = "Teclado", description = "USB", category = "ELECTRONICA", imageUrl = "https://example.com/teclado.jpg", ownerId = cognitoId)
        val swipeRequest = SwipeRequest(targetItemId = 2L, type = SwipeType.LIKE, offeredItemId = 99L)

        `when`(itemRepository.findById(2L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(cognitoId)).thenReturn(listOf(userItem))

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(cognitoId, swipeRequest)
        }
    }

    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when user has multiple items and none specified`() {
        val targetItem = Item(id = 2L, title = "Silla", description = "Gamer", category = "MUEBLES", imageUrl = "https://example.com/silla.jpg", ownerId = "other-user")
        val userItems = listOf(
            Item(id = 10L, title = "Teclado", description = "USB", category = "ELECTRONICA", imageUrl = "https://example.com/t.jpg", ownerId = cognitoId),
            Item(id = 11L, title = "Mouse", description = "Gamer", category = "ELECTRONICA", imageUrl = "https://example.com/m.jpg", ownerId = cognitoId)
        )
        val swipeRequest = SwipeRequest(targetItemId = 2L, type = SwipeType.LIKE, offeredItemId = null)

        `when`(itemRepository.findById(2L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(cognitoId)).thenReturn(userItems)

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(cognitoId, swipeRequest)
        }
    }

    @Test
    fun `processSwipe auto selects item when offeredItemId is null and user has single item`() {
        val targetItem = Item(id = 2L, title = "Silla", description = "Gamer", category = "MUEBLES", imageUrl = "https://example.com/silla.jpg", ownerId = "other-user")
        val userItem = Item(id = 10L, title = "Teclado", description = "USB", category = "ELECTRONICA", imageUrl = "https://example.com/teclado.jpg", ownerId = cognitoId)
        val swipeRequest = SwipeRequest(targetItemId = 2L, type = SwipeType.LIKE, offeredItemId = null)
        val savedMatch = Match(id = 101L, user1Id = cognitoId, user2Id = "other-user", status = "PENDING")

        `when`(itemRepository.findById(2L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(cognitoId)).thenReturn(listOf(userItem))
        `when`(matchRepository.save(any(Match::class.java))).thenReturn(savedMatch)

        val response = matchService.processSwipe(cognitoId, swipeRequest)

        assertTrue(response.isMatch)
        assertEquals(101L, response.matchId)
    }

    @Test
    fun `updateMatchStatus throws MatchNotFoundException when match id does not exist`() {
        val updateDto = UpdateMatchStatusDto(status = MatchStatus.APPROVED)
        `when`(matchRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<MatchNotFoundException> {
            matchService.updateMatchStatus(99L, cognitoId, updateDto)
        }
    }
}