package com.pucetec.swipeshare.controllers

import com.pucetec.swipeshare.dto.ItemRequest
import com.pucetec.swipeshare.dto.ItemResponse
import com.pucetec.swipeshare.services.ItemService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
class ItemController(
    val itemService: ItemService
) {
    private val logger = LoggerFactory.getLogger(ItemController::class.java)

    @PostMapping("/api/items")
    fun createItem(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: ItemRequest
    ): ItemResponse {
        val cognitoId = jwt.subject!!
        logger.info("User $cognitoId is creating a new item")
        return itemService.createItem(cognitoId, request)
    }

    @GetMapping("/api/items")
    fun getAllItems(): List<ItemResponse> {
        logger.info("Getting all available items")
        return itemService.getAllItems()
    }

    @GetMapping("/api/items/{id}")
    fun getItemById(@PathVariable id: Long): ItemResponse {
        logger.info("Getting item with id: $id")
        return itemService.getItemById(id)
    }

    @DeleteMapping("/api/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteItem(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long
    ) {
        val cognitoId = jwt.subject!!
        logger.info("User $cognitoId attempting to delete item $id")
        itemService.deleteItem(id, cognitoId)
    }
    @GetMapping("/api/items/me")
    fun getMyItems(
        @AuthenticationPrincipal jwt: Jwt
    ): List<ItemResponse> {
        val cognitoId = jwt.subject!!
        logger.info("User $cognitoId is fetching their own items")
        return itemService.getItemsByCognitoId(cognitoId)
    }
}