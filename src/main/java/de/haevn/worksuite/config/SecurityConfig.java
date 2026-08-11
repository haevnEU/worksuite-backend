package de.haevn.worksuite.config;

import de.haevn.worksuite.config.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration establishing stateless JWT-based authentication.
 *
 * <p>Secures all API routes with {@link JwtAuthenticationFilter} while whitelisting public documentation,
 * WebSocket, and shared resource endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**",
        "/v3/api-docs",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-ui/index.html",
        "/swagger-resources/**",
        "/webjars/**"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/ws/**",
        "/api/v1/share/**",
        "/api/v1/about/**",
        "/actuator/health"
    };

    private final String jwtSecret;

    /**
     * Constructs security configuration with the injected JWT signing secret.
     *
     * @param jwtSecret the signing secret configured via {@code ${jwt.secret}}
     */
    public SecurityConfig(@Value("${jwt.secret}") final String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    /**
     * Builds the {@link JwtAuthenticationFilter} bean.
     *
     * @return configured {@link JwtAuthenticationFilter}
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthFilter() {
        return new JwtAuthenticationFilter(jwtSecret);
    }

    /**
     * Configures the main Spring Security HTTP filter chain.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the compiled {@link SecurityFilterChain}
     * @throws Exception if security filter configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow unauthenticated CORS preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Whitelist Swagger UI, OpenAPI specifications, and static assets
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                // Whitelist public endpoints
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                // Authenticate all remaining routes
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}