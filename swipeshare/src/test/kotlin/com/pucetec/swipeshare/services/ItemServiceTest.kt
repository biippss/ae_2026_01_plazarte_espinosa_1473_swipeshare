package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.entities.Item
import com.pucetec.swipeshare.entities.Match
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.UnauthorizedItemAccessException
import com.pucetec.swipeshare.repositories.ItemRepository
import com.pucetec.swipeshare.repositories.MatchRepository
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
class ItemServiceTest {

    @Mock
    private lateinit var itemRepository: ItemRepository

    @Mock
    private lateinit var matchRepository: MatchRepository

    @InjectMocks
    private lateinit var itemService: ItemService

    private val cognitoId = "user-cognito-123"

    @Test
    fun `createItem saves item successfully`() {
        val request = ItemRequest(
            title = "Balón de fútbol",
            description = "En buen estado",
            category = "DEPORTES",
            imageUrl = "http://example.com/balon.jpg"
        )
        val savedItem = Item(
            id = 1L,
            title = "Balón de fútbol",
            description = "En buen estado",
            category = "DEPORTES",
            imageUrl = "http://example.com/balon.jpg",
            ownerId = cognitoId
        )

        `when`(itemRepository.save(any(Item::class.java))).thenReturn(savedItem)

        val response = itemService.createItem(cognitoId, request)

        assertEquals(1L, response.id)
        assertEquals("Balón de fútbol", response.title)
        assertEquals(cognitoId, response.ownerId)
    }

    @Test
    fun `getAllItemsExceptUser filters out items owned by user and matched items`() {
        val item1 = Item(id = 1L, title = "Mesa", description = "", category = "", ownerId = "other-user")
        val item2 = Item(id = 2L, title = "Silla", description = "", category = "", ownerId = "other-user")
        val approvedMatch = Match(id = 10L, user1Id = "other-user", user2Id = "another-user", offeredItemId = 1L, requestedItemId = 99L, status = "APPROVED")

        `when`(itemRepository.findByOwnerIdNot(cognitoId)).thenReturn(listOf(item1, item2))
        `when`(matchRepository.findByStatus("APPROVED")).thenReturn(listOf(approvedMatch))

        val results = itemService.getAllItemsExceptUser(cognitoId)

        assertEquals(1, results.size)
        assertEquals(2L, results[0].id)
        assertEquals("Silla", results[0].title)
    }

    @Test
    fun `getItemById returns item when found`() {
        val item = Item(id = 5L, title = "Laptop", description = "Gaming", category = "TECH", ownerId = cognitoId)
        `when`(itemRepository.findById(5L)).thenReturn(Optional.of(item))

        val response = itemService.getItemById(5L)

        assertEquals(5L, response.id)
        assertEquals("Laptop", response.title)
    }

    @Test
    fun `getItemById throws ItemNotFoundException when item missing`() {
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> {
            itemService.getItemById(99L)
        }
    }

    @Test
    fun `deleteItem deletes item when user is owner`() {
        val item = Item(id = 5L, title = "Mouse", description = "", category = "", ownerId = cognitoId)
        `when`(itemRepository.findById(5L)).thenReturn(Optional.of(item))

        itemService.deleteItem(5L, cognitoId)

        verify(itemRepository).delete(item)
    }

    @Test
    fun `deleteItem throws ItemNotFoundException when item does not exist`() {
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> {
            itemService.deleteItem(99L, cognitoId)
        }
    }

    @Test
    fun `deleteItem throws UnauthorizedItemAccessException when user is not owner`() {
        val item = Item(id = 5L, title = "Mouse", description = "", category = "", ownerId = "other-owner")
        `when`(itemRepository.findById(5L)).thenReturn(Optional.of(item))

        assertThrows<UnauthorizedItemAccessException> {
            itemService.deleteItem(5L, cognitoId)
        }
    }

    @Test
    fun `getItemsByCognitoId returns user items`() {
        val items = listOf(
            Item(id = 1L, title = "Camiseta", description = "", category = "", ownerId = cognitoId)
        )
        `when`(itemRepository.findByOwnerId(cognitoId)).thenReturn(items)

        val results = itemService.getItemsByCognitoId(cognitoId)

        assertEquals(1, results.size)
        assertEquals("Camiseta", results[0].title)
    }

    @Test
    fun `updateItem updates item fields when user is owner`() {
        val item = Item(id = 1L, title = "Viejo Titulo", description = "Vieja Desc", category = "OLD", ownerId = cognitoId)
        val request = ItemRequest(title = "Nuevo Titulo", description = "Nueva Desc", category = "NEW", imageUrl = "http://img.jpg")
        val updatedItem = Item(id = 1L, title = "Nuevo Titulo", description = "Nueva Desc", category = "NEW", imageUrl = "http://img.jpg", ownerId = cognitoId)

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(item))
        `when`(itemRepository.save(any(Item::class.java))).thenReturn(updatedItem)

        val response = itemService.updateItem(1L, cognitoId, request)

        assertEquals("Nuevo Titulo", response.title)
        assertEquals("Nueva Desc", response.description)
    }

    @Test
    fun `updateItem throws ItemNotFoundException when item missing`() {
        val request = ItemRequest(title = "Nuevo", description = "Nueva", category = "CAT", imageUrl = null)
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> {
            itemService.updateItem(99L, cognitoId, request)
        }
    }

    @Test
    fun `updateItem throws UnauthorizedItemAccessException when user is not owner`() {
        val item = Item(id = 1L, title = "Titulo", description = "Desc", category = "OLD", ownerId = "other-owner")
        val request = ItemRequest(title = "Nuevo Titulo", description = "Nueva Desc", category = "NEW", imageUrl = null)

        `when`(itemRepository.findById(1L)).thenReturn(Optional.of(item))

        assertThrows<UnauthorizedItemAccessException> {
            itemService.updateItem(1L, cognitoId, request)
        }
    }
}