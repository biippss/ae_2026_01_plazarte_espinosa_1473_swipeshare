package com.pucetec.users.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class HttpLoggingFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(HttpLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val method = request.method
        val uri = request.requestURI

        logger.info("event=http.request | msg={} {}", method, uri)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val status = response.status
            logger.info("event=http.response | msg={} {} {}", status, method, uri)
        }
    }
}