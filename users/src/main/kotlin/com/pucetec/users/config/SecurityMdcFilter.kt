package com.pucetec.users.config
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SecurityMdcFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val sub = extractSubFromHeader(request.getHeader("Authorization"))
        MDC.put("sub", sub)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("sub")
        }
    }

    private fun extractSubFromHeader(authHeader: String?): String {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return "anonimo"
        return try {
            val token = authHeader.substring(7)
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.getUrlDecoder().decode(parts[1]))
                val match = """"sub"\s*:\s*"([^"]+)"""".toRegex().find(payload)
                match?.groupValues?.get(1) ?: "anonimo"
            } else "anonimo"
        } catch (e: Exception) {
            "anonimo"
        }
    }
}