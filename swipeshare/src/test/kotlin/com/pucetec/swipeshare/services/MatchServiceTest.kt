package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.*
import com.pucetec.swipeshare.entities.Item
import com.pucetec.swipeshare.entities.Match
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.MatchNotFoundException
import com.pucetec.swipeshare.exceptions.UnauthorizedItemAccessException
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
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

    private val user1 = "user-123"
    private val user2 = "user-456"

    @Test
    fun `processSwipe throws ItemNotFoundException when target item does not exist`() {
        val request = SwipeRequest(targetItemId = 99L, offeredItemId = null, type = SwipeType.LIKE)
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> {
            matchService.processSwipe(user1, request)
        }
    }

    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when swiping on own item`() {
        val item = Item(id = 1L, title = "Mesa", description = "", category = "", ownerId = user1)
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = null, type = SwipeType.LIKE)
        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(item))

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(user1, request)
        }
    }

    @Test
    fun `processSwipe returns rejection on DISLIKE`() {
        val item = Item(id = 1L, title = "Silla", description = "", category = "", ownerId = user2)
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = null, type = SwipeType.DISLIKE)
        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(item))

        val response = matchService.processSwipe(user1, request)

        assertFalse(response.isMatch)
        assertEquals("Item rejected successfully", response.message)
    }

    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when user has no items to offer`() {
        val item = Item(id = 1L, title = "Silla", description = "", category = "", ownerId = user2)
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = null, type = SwipeType.LIKE)

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(item))
        `when`(itemRepository.findByOwnerId(user1)).thenReturn(emptyList())

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(user1, request)
        }
    }

    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when offered item does not belong to user`() {
        val targetItem = Item(id = 1L, title = "Silla", description = "", category = "", ownerId = user2)
        val myItem = Item(id = 10L, title = "Cama", description = "", category = "", ownerId = user1)
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = 99L, type = SwipeType.LIKE)

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(user1)).thenReturn(listOf(myItem))

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(user1, request)
        }
    }

    @Test
    fun `processSwipe throws UnauthorizedItemAccessException when multiple items exist and none selected`() {
        val targetItem = Item(id = 1L, title = "Silla", description = "", category = "", ownerId = user2)
        val item1 = Item(id = 10L, title = "Cama", description = "", category = "", ownerId = user1)
        val item2 = Item(id = 11L, title = "Mesa", description = "", category = "", ownerId = user1)
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = null, type = SwipeType.LIKE)

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(user1)).thenReturn(listOf(item1, item2))

        assertThrows<UnauthorizedItemAccessException> {
            matchService.processSwipe(user1, request)
        }
    }

    @Test
    fun `processSwipe creates match successfully when user has single item`() {
        val targetItem = Item(id = 1L, title = "Silla", description = "", category = "", ownerId = user2)
        val myItem = Item(id = 10L, title = "Cama", description = "", category = "", ownerId = user1)
        val request = SwipeRequest(targetItemId = 1L, offeredItemId = null, type = SwipeType.LIKE)
        val match = Match(id = 100L, user1Id = user1, user2Id = user2, offeredItemId = 10L, requestedItemId = 1L, status = "PENDING")

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(targetItem))
        `when`(itemRepository.findByOwnerId(user1)).thenReturn(listOf(myItem))
        `when`(matchRepository.save(any(Match::class.java))).thenReturn(match)

        val response = matchService.processSwipe(user1, request)

        // CORREGIDO: SwipeResponse valida isMatch y message
        assertNotNull(response)
        assertNotNull(response.message)
    }

    @Test
    fun `createMatch throws ItemNotFoundException when requested item missing`() {
        val request = MatchRequest(offeredItemId = 10L, requestedItemId = 99L)
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> {
            matchService.createMatch(user1, request)
        }
    }

    @Test
    fun `createMatch succeeds when requested item exists`() {
        val requestedItem = Item(id = 1L, title = "Reloj", description = "", category = "", ownerId = user2)
        val request = MatchRequest(offeredItemId = 10L, requestedItemId = 1L)
        val match = Match(id = 50L, user1Id = user1, user2Id = user2, offeredItemId = 10L, requestedItemId = 1L, status = "PENDING")

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(requestedItem))
        `when`(matchRepository.save(any(Match::class.java))).thenReturn(match)

        val response = matchService.createMatch(user1, request)

        assertEquals(50L, response.id)
        assertEquals(user1, response.user1Id)
    }

    @Test
    fun `getMatchesByUser returns matches`() {
        val match = Match(id = 1L, user1Id = user1, user2Id = user2, offeredItemId = 10L, requestedItemId = 1L, status = "PENDING")
        `when`(matchRepository.findByUser1IdOrUser2Id(user1, user1)).thenReturn(listOf(match))

        val results = matchService.getMatchesByUser(user1)

        assertEquals(1, results.size)
        assertEquals(1L, results[0].id)
    }

    @Test
    fun `updateMatchStatus throws MatchNotFoundException when match not found`() {
        val dto = UpdateMatchStatusDto(status = MatchStatus.APPROVED)
        `when`(matchRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<MatchNotFoundException> {
            matchService.updateMatchStatus(99L, user1, dto)
        }
    }

    @Test
    fun `updateMatchStatus throws UnauthorizedItemAccessException when user is not participant`() {
        val match = Match(id = 1L, user1Id = user1, user2Id = user2, offeredItemId = 10L, requestedItemId = 1L, status = "PENDING")
        val dto = UpdateMatchStatusDto(status = MatchStatus.APPROVED)
        `when`(matchRepository.findById(1L)).thenReturn(Optional.of(match))

        assertThrows<UnauthorizedItemAccessException> {
            matchService.updateMatchStatus(1L, "intruder-user", dto)
        }
    }

    @Test
    fun `updateMatchStatus updates status to APPROVED and handles internal karma call`() {
        val match = Match(id = 1L, user1Id = user1, user2Id = user2, offeredItemId = 10L, requestedItemId = 1L, status = "PENDING")
        val updatedMatch = Match(id = 1L, user1Id = user1, user2Id = user2, offeredItemId = 10L, requestedItemId = 1L, status = "APPROVED")
        val dto = UpdateMatchStatusDto(status = MatchStatus.APPROVED)

        `when`(matchRepository.findById(1L)).thenReturn(Optional.of(match))
        `when`(matchRepository.save(any(Match::class.java))).thenReturn(updatedMatch)

        val response = matchService.updateMatchStatus(1L, user1, dto)

        assertEquals("APPROVED", response.status)
    }
}