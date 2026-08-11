package de.haevn.worksuite.config.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that parses and authenticates incoming Bearer JWT tokens in the {@code Authorization} header.
 *
 * <p>Populates the Spring {@link SecurityContextHolder} upon successful cryptographic validation.
 *
 * <p>Example header:
 * <pre>{@code
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * }</pre>
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CLAIM_ROLE = "role";
    private static final String ROLE_PREFIX = "ROLE_";

    private final SecretKey signingKey;

    /**
     * Constructs the filter using a raw HMAC secret string.
     *
     * @param secretKey the shared secret key used for HMAC signature validation
     */
    public JwtAuthenticationFilter(final String secretKey) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Inspects incoming request headers, validates the JWT, and sets security authentication context.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @param filterChain servlet filter chain
     * @throws ServletException if an error occurs during filter processing
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain)
        throws ServletException, IOException {

        extractBearerToken(request).ifPresent(jwt -> authenticateToken(jwt, request));
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw Bearer JWT token from the {@link HttpHeaders#AUTHORIZATION} header.
     *
     * <p>Example:
     * <pre>{@code
     * Optional<String> token = extractBearerToken(request);
     * }</pre>
     *
     * @param request incoming HTTP servlet request
     * @return an {@link Optional} containing the raw token, or empty if header is missing/invalid
     */
    private Optional<String> extractBearerToken(final HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return Optional.of(authHeader.substring(BEARER_PREFIX.length()).trim());
        }
        return Optional.empty();
    }

    /**
     * Validates claims within the JWT and establishes the {@link UsernamePasswordAuthenticationToken}.
     *
     * <p>Example:
     * <pre>{@code
     * authenticateToken("eyJhbGciOi...", request);
     * }</pre>
     *
     * @param jwt the raw JWT string
     * @param request incoming HTTP servlet request
     */
    private void authenticateToken(final String jwt, final HttpServletRequest request) {
        try {
            final Claims claims = Jwts.parser().verifyWith(this.signingKey).build().parseSignedClaims(jwt).getPayload();

            final String userId = claims.getSubject();
            final String role = claims.get(CLAIM_ROLE, String.class);

            if (StringUtils.hasText(userId) && SecurityContextHolder.getContext().getAuthentication() == null) {
                final List<SimpleGrantedAuthority> authorities = StringUtils.hasText(role) ?
                    List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role)) :
                    Collections.emptyList();

                final UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token rejected: {}", ex.getMessage());
        }
    }
}