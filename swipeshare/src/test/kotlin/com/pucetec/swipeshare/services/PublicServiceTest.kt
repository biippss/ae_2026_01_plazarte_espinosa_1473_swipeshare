package com.pucetec.swipeshare.services

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
    fun `getGlobalStats returns global counts for items and matches`() {
        `when`(itemRepository.count()).thenReturn(15L)
        `when`(matchRepository.count()).thenReturn(5L)

        val response = publicService.getGlobalStats()

        assertEquals(15L, response.totalItems)
        assertEquals(5L, response.totalMatches)
    }
}