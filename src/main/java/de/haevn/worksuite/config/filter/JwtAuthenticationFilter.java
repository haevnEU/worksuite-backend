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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CLAIM_ROLE = "role";
    private static final String ROLE_PREFIX = "ROLE_";

    private final SecretKey signingKey;

    public JwtAuthenticationFilter(final String secretKey) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain)
        throws ServletException, IOException {

        final Optional<String> tokenOptional = extractBearerToken(request);

        if (tokenOptional.isPresent()) {
            final boolean authenticated = authenticateToken(tokenOptional.get(), request);

            // Token war vorhanden, aber ungültig -> Sofort 401 senden und FilterChain abbrechen
            if (!authenticated) {
                sendUnauthorizedError(response, "Invalid or expired JWT token");
                return;
            }
        }

        // Kein Token vorhanden (z. B. Public Endpoints) oder Token war gültig -> Kette fortsetzen
        filterChain.doFilter(request, response);
    }

    private Optional<String> extractBearerToken(final HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return Optional.of(authHeader.substring(BEARER_PREFIX.length()).trim());
        }
        return Optional.empty();
    }

    private boolean authenticateToken(final String jwt, final HttpServletRequest request) {
        try {
            final Claims claims = Jwts.parser()
                .verifyWith(this.signingKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

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
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token rejected: {}", ex.getMessage());
            return false;
        }
    }

    private void sendUnauthorizedError(final HttpServletResponse response, final String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\"");
        response.getWriter().write(String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
    }
}