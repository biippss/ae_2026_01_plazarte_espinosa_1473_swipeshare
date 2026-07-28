package com.pucetec.swipeshare.dto

/***
 * Lo que devuelve el micro en el endpoint público /api/public/stats
 * {totalUsers: 150, totalItemsExchanged: 320, activeMatches: 45}
 */
data class StatsResponse(
    val totalUsers: Int,
    val totalItemsExchanged: Int,
    val activeMatches: Int
)