package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.entities.Item
import com.pucetec.swipeshare.exceptions.ItemNotFoundException
import com.pucetec.swipeshare.exceptions.UnauthorizedItemAccessException
import com.pucetec.swipeshare.repositories.ItemRepository
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

    @InjectMocks
    private lateinit var itemService: ItemService

    private val cognitoId = "sub-cognito-123"

    @Test
    fun `createItem associates cognitoId and saves item successfully`() {
        val request = ItemRequest(
            title = "Libro de Kotlin",
            description = "Usado en buen estado",
            category = "LIBROS",
            imageUrl = "https://example.com/kotlin.jpg"
        )
        val savedItem = Item(
            id = 1L,
            title = "Libro de Kotlin",
            description = "Usado en buen estado",
            category = "LIBROS",
            imageUrl = "https://example.com/kotlin.jpg",
            ownerId = cognitoId
        )

        `when`(itemRepository.save(any(Item::class.java))).thenReturn(savedItem)

        val response = itemService.createItem(cognitoId, request)

        assertEquals(1L, response.id)
        assertEquals("Libro de Kotlin", response.title)
        assertEquals(cognitoId, response.ownerId)
    }

    @Test
    fun `getAllItemsExceptUser returns items not belonging to current user`() {
        val cognitoId = "user-123"
        val items = listOf(
            Item(id = 1L, title = "Calculadora", description = "Casi nueva", category = "Electronica", ownerId = "other-user"),
            Item(id = 2L, title = "Teclado", description = "Mecanico", category = "Electronica", ownerId = "other-user")
        )

        `when`(itemRepository.findByOwnerIdNot(cognitoId)).thenReturn(items)

        val responses = itemService.getAllItemsExceptUser(cognitoId)

        assertEquals(2, responses.size)
        assertEquals("Calculadora", responses[0].title)
    }

    @Test
    fun `getAllItemsExceptUser excludes items owned by current user`() {
        val cognitoId = "user-123"
        val otherItems = listOf(
            Item(id = 2L, title = "Teclado", description = "Mecanico", category = "Electronica", ownerId = "other-user")
        )

        `when`(itemRepository.findByOwnerIdNot(cognitoId)).thenReturn(otherItems)

        val responses = itemService.getAllItemsExceptUser(cognitoId)

        assertEquals(1, responses.size)
        assertEquals("Teclado", responses[0].title)
    }

    @Test
    fun `getItemById returns item when it exists`() {
        val item = Item(id = 10L, title = "Audifonos", description = "Sony", category = "AUDIO", imageUrl = "https://example.com/sony.jpg", ownerId = cognitoId)
        `when`(itemRepository.findById(10L)).thenReturn(Optional.of(item))

        val response = itemService.getItemById(10L)

        assertEquals(10L, response.id)
        assertEquals("Audifonos", response.title)
    }

    @Test
    fun `getItemById throws ItemNotFoundException when id does not exist`() {
        `when`(itemRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ItemNotFoundException> {
            itemService.getItemById(99L)
        }
    }

    @Test
    fun `deleteItem deletes item when user is owner`() {
        val item = Item(id = 5L, title = "Mouse", description = "Logitech", category = "ELECTRONICA", imageUrl = "https://example.com/mouse.jpg", ownerId = cognitoId)
        `when`(itemRepository.findById(5L)).thenReturn(Optional.of(item))

        itemService.deleteItem(5L, cognitoId)

        verify(itemRepository).delete(item)
    }

    @Test
    fun `deleteItem throws UnauthorizedItemAccessException when item belongs to another user`() {
        val item = Item(id = 5L, title = "Mouse", description = "Logitech", category = "ELECTRONICA", imageUrl = "https://example.com/mouse.jpg", ownerId = "otro-dueno")
        `when`(itemRepository.findById(5L)).thenReturn(Optional.of(item))

        assertThrows<UnauthorizedItemAccessException> {
            itemService.deleteItem(5L, cognitoId)
        }
    }
    @Test
    fun `getItemsByCognitoId returns items owned by user`() {
        val items = listOf(
            Item(id = 1L, title = "Lapiz", description = "Verde", category = "VARIOS", imageUrl = "https://example.com/lapiz.jpg", ownerId = cognitoId)
        )
        `when`(itemRepository.findByOwnerId(cognitoId)).thenReturn(items)

        val responses = itemService.getItemsByCognitoId(cognitoId)

        assertEquals(1, responses.size)
        assertEquals("Lapiz", responses[0].title)
    }
}