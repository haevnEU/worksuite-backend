package de.haevn.worksuite.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that establishes and propagates a unique correlation ID across the request lifecycle.
 *
 * <p>Inspects incoming {@code X-Correlation-ID} HTTP headers or generates a new {@link UUID}, populating both
 * the SLF4J/Log4j2 {@link MDC} and the outgoing response header.
 *
 * <p>Example flow:
 * <pre>{@code
 * // Incoming: GET /api/v1/resource (Header: X-Correlation-ID: abc-123)
 * // Filter action: MDC.put("correlationId", "abc-123")
 * // Outgoing: Response contains Header X-Correlation-ID: abc-123
 * }</pre>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    /**
     * Intercepts requests to set up logging context and response headers with the resolved correlation ID.
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
        final String correlationId = resolveCorrelationId(request);

        try {
            MDC.put(MDC_KEY, correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /**
     * Resolves the correlation ID from the request header or generates a fallback {@link UUID}.
     *
     * <p>Example:
     * <pre>{@code
     * String id = resolveCorrelationId(request);
     * }</pre>
     *
     * @param request the incoming {@link HttpServletRequest}
     * @return the extracted correlation ID or a new random UUID string
     */
    private String resolveCorrelationId(final HttpServletRequest request) {
        final String headerValue = request.getHeader(CORRELATION_ID_HEADER);
        return StringUtils.hasText(headerValue) ? headerValue.trim() : UUID.randomUUID().toString();
    }
}