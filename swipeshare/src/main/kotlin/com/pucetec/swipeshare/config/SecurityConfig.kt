package com.pucetec.swipeshare.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val securityMdcFilter: SecurityMdcFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.GET, "/api/public/stats").permitAll()
                auth.requestMatchers("/api/admin", "/api/admin/**").hasRole("ADMIN")
                auth.requestMatchers("/api/items", "/api/items/**").hasRole("USER")
                auth.requestMatchers("/api/matches", "/api/matches/**").hasRole("USER")
                auth.requestMatchers("/api/swipes", "/api/swipes/**").hasRole("USER")
                auth.requestMatchers("/api/reviews", "/api/reviews/**").hasRole("USER")
                auth.requestMatchers("/api/users", "/api/users/**").hasAnyRole("USER", "ADMIN")
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { it.jwtAuthenticationConverter(cognitoGroupsConverter()) }
            }
            .addFilterAfter(securityMdcFilter, BearerTokenAuthenticationFilter::class.java)

        return http.build()
    }

    private fun cognitoGroupsConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val groups = jwt.getClaimAsStringList("cognito:groups") ?: emptyList()

            groups.map { group ->
                val authorityName = if (group.startsWith("ROLE_")) group else "ROLE_$group"
                SimpleGrantedAuthority(authorityName)
            }
        }
        return converter
    }
}