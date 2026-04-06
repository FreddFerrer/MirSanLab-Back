package com.mirsanlab.backend.security;

import com.mirsanlab.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String corsAllowedOrigins;

    @Value("${app.cors.allowed-origin-patterns:}")
    private String corsAllowedOriginPatterns;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String corsAllowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String corsAllowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean corsAllowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long corsMaxAge;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors
                        .configurationSource(request -> {
                            var config = new org.springframework.web.cors.CorsConfiguration();
                            List<String> originPatterns = splitCsv(corsAllowedOriginPatterns);
                            if (!originPatterns.isEmpty()) {
                                config.setAllowedOriginPatterns(originPatterns);
                            } else {
                                config.setAllowedOrigins(splitCsv(corsAllowedOrigins));
                            }
                            config.setAllowedMethods(splitCsv(corsAllowedMethods));
                            config.setAllowedHeaders(splitCsv(corsAllowedHeaders));
                            config.setAllowCredentials(corsAllowCredentials);
                            config.setMaxAge(corsMaxAge);
                            return config;
                        })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private List<String> splitCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("""
            {
              "statusCode": 403,
              "message": "Acceso denegado",
              "path": "%s"
            }
            """.formatted(request.getRequestURI()));
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("""
            {
              "statusCode": 401,
              "message": "No autorizado",
              "path": "%s"
            }
            """.formatted(request.getRequestURI()));
        };
    }

}
