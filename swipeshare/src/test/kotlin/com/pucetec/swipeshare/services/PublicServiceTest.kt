package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.entities.Item
import com.pucetec.swipeshare.entities.Match
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PublicServiceTest {

    @Mock
    private lateinit var itemRepository: ItemRepository

    @Mock
    private lateinit var matchRepository: MatchRepository

    @InjectMocks
    private lateinit var publicService: PublicService

    @Test
    fun `getGlobalStats calculates active items excluding matched items and total approved matches`() {
        val approvedMatch = Match(id = 1L, user1Id = "u1", user2Id = "u2", offeredItemId = 10L, requestedItemId = 20L, status = "APPROVED")
        val pendingMatch = Match(id = 2L, user1Id = "u3", user2Id = "u4", offeredItemId = 11L, requestedItemId = 21L, status = "PENDING")

        val item1 = Item(id = 10L, title = "Balon", description = "", category = "", ownerId = "u1")
        val item2 = Item(id = 30L, title = "Raqueta", description = "", category = "", ownerId = "u5")

        `when`(matchRepository.findByStatus("APPROVED")).thenReturn(listOf(approvedMatch))
        `when`(itemRepository.findAll()).thenReturn(listOf(item1, item2))

        val stats = publicService.getGlobalStats()

        assertEquals(1L, stats.totalItems)
        assertEquals(1L, stats.totalMatches)
    }
}