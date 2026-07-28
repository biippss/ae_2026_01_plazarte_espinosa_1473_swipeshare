package com.pucetec.swipeshare.dto

/***
 * Lo que envia el cliente al publicar un nuevo objeto para intercambio en la app.
 * {title: "Laptop Acer Nitro", description: "En perfecto estado", category: "Electrónica", imageUrl: "https://..."}
 */
data class ItemRequest(
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String?
)

/***
 * Lo que devuelve el micro: el objeto con su ID generado y el ID del estudiante dueño.
 * {id: 10, title: "Laptop Acer Nitro", description: "En perfecto estado", category: "Electrónica", imageUrl: "https://...", ownerId: 1}
 */
data class ItemResponse(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String?,
    val ownerId: Long
)