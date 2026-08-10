package com.pucetec.swipeshare.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SecurityMdcFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        val sub = if (authentication != null && authentication.isAuthenticated) {
            authentication.name
        } else {
            "anonimo"
        }

        MDC.put("sub", sub)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("sub")
        }
    }
}