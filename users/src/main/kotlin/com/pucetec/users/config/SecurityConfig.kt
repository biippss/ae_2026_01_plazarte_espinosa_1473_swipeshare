package com.pucetec.users.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                // 1. Endpoints públicos
                auth.requestMatchers("/api/public/**").permitAll()

                // 2. Endpoints de administración de usuarios (solo ADMIN)
                auth.requestMatchers("/api/users/admin/**").hasRole("ADMIN")

                // 3. Endpoints de perfil y gestión de usuarios (USER o ADMIN)
                auth.requestMatchers("/api/users", "/api/users/**").hasAnyRole("USER", "ADMIN")

                // 4. Cualquier otra ruta requiere autenticación por defecto
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(cognitoGroupsConverter())
                }
            }

        return http.build()
    }

    private fun cognitoGroupsConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val groups = jwt.getClaimAsStringList("cognito:groups") ?: emptyList()

            groups.map { group ->
                // Mapea los grupos de Cognito agregando el prefijo ROLE_ exigido por Spring Security
                val authorityName = if (group.startsWith("ROLE_")) group else "ROLE_$group"
                SimpleGrantedAuthority(authorityName)
            }
        }
        return converter
    }
}