package com.pucetec.swipeshare.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                // 1. Endpoint público para el inicio de sesión (Cualquiera puede verlo)
                auth.requestMatchers(HttpMethod.GET, "/api/public/stats").permitAll()

                // 2. Endpoints exclusivos para Administración
                auth.requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 3. Endpoints para el uso de la app móvil (Estudiantes)
                auth.requestMatchers("/api/items/**").hasRole("STUDENT")
                auth.requestMatchers("/api/matches/**").hasRole("STUDENT")
                auth.requestMatchers("/api/reviews/**").hasRole("STUDENT")

                // 4. Perfiles de usuario (Permitimos a ambos roles consultar usuarios)
                auth.requestMatchers("/api/users/**").hasAnyRole("STUDENT", "ADMIN")

                // 5. Cualquier otra ruta requerirá autenticación por defecto
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { it.jwtAuthenticationConverter(cognitoGroupsConverter()) }
            }

        return http.build()
    }

    private fun cognitoGroupsConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val groups = jwt.getClaimAsStringList("cognito:groups") ?: emptyList()
            groups.map { SimpleGrantedAuthority("ROLE_$it") }
        }
        return converter
    }
}