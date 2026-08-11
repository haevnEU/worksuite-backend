package de.haevn.worksuite.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Spring MVC interceptor tracking invocation counts and last-access timestamps for matched API route patterns.
 *
 * <p>Captures the normalized pattern from {@link HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE} to ensure
 * dynamic URL path parameters (e.g. {@code /api/v1/notes/{id}}) are incremented against the abstract route entry
 * rather than unique entity IDs.
 *
 * <p>Example execution flow:
 * <pre>{@code
 * // Request executes: GET /api/v1/notes/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * // Handler completes -> afterCompletion extracts "/api/v1/notes/{id}"
 * // Database updates: invocation_count += 1, last_invoked_at = now()
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteUsageTrackingInterceptor implements HandlerInterceptor {

    private static final String PARAM_HTTP_METHOD = "httpMethod";
    private static final String PARAM_PATTERN = "pattern";

    private static final String INCREMENT_SQL = """
        UPDATE route_usage_metrics 
        SET invocation_count = invocation_count + 1,
            last_invoked_at = CURRENT_TIMESTAMP
        WHERE http_method = :httpMethod 
          AND pattern = :pattern;
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Intercepts completed requests to record invocation metrics for matched route patterns.
     *
     * @param request the current {@link HttpServletRequest}
     * @param response the current {@link HttpServletResponse}
     * @param handler the chosen handler object
     * @param ex any uncaught exception thrown during handler execution
     */
    @Override
    public void afterCompletion(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final Object handler,
        @Nullable final Exception ex
    ) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return;
        }

        final Object bestMatchingPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestMatchingPattern instanceof String pattern) {
            recordInvocation(request.getMethod(), pattern);
        }
    }

    /**
     * Executes the SQL update statement to increment the invocation counter and update the timestamp.
     *
     * <p>Example usage:
     * <pre>{@code
     * recordInvocation("GET", "/api/v1/users/{id}");
     * }</pre>
     *
     * @param httpMethod the HTTP request method name (e.g., {@code GET}, {@code POST})
     * @param pattern the resolved URL path pattern
     */
    private void recordInvocation(final String httpMethod, final String pattern) {
        Objects.requireNonNull(httpMethod, "HTTP method must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");

        try {
            final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PARAM_HTTP_METHOD, httpMethod)
                .addValue(PARAM_PATTERN, pattern);

            jdbcTemplate.update(INCREMENT_SQL, params);
        } catch (final Exception ex) {
            log.warn("Failed to record usage metric for route [{} {}]: {}", httpMethod, pattern, ex.getMessage());
        }
    }
}