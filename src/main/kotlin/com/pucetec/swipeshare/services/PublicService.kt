package com.pucetec.swipeshare.services

import com.pucetec.swipeshare.dto.StatsResponse
import org.springframework.stereotype.Service

@Service
class PublicService(
    private val userService: UserService
) {
    fun getGlobalStats(): StatsResponse = userService.getGlobalStats()
}