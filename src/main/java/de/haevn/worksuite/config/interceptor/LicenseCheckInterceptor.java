package de.haevn.worksuite.config.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import de.haevn.worksuite.settings.LicenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that validates the subscription and workspace license status of the active user.
 *
 * <p>Inspects the authenticated principal from {@link SecurityContextHolder}, checks validity with
 * {@link LicenseService}, and immediately rejects requests with an HTTP 402 Payment Required response
 * using {@link ErrorResponseDTO} if the license has expired.
 *
 * <p>Example execution flow:
 * <pre>{@code
 * // 1. Request arrives from authenticated user UUID.
 * // 2. LicenseCheckInterceptor queries licenseService.licenseExpired(userId).
 * // 3. If expired: emits HTTP 402 JSON payload and aborts the handler chain.
 * // 4. If active: continues downstream execution.
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseCheckInterceptor implements HandlerInterceptor {

    private static final String ANONYMOUS_PRINCIPAL = "anonymousUser";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String LICENSE_EXPIRED_MESSAGE =
        "Your workspace license has expired. Please renew your subscription to continue.";

    private final LicenseService licenseService;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts execution before reaching the controller handler to perform license validation.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @param handler chosen handler to execute
     * @return {@code true} if access is permitted; {@code false} if rejected due to an expired license
     * @throws Exception if writing the error response fails
     */
    @Override
    public boolean preHandle(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
        @NonNull final Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        final Optional<UUID> userIdOpt = resolveCurrentUserId();
        if (userIdOpt.isEmpty()) {
            return true;
        }

        final UUID userId = userIdOpt.get();
        if (licenseService.licenseExpired(userId)) {
            log.warn("Access denied for user {}: Workspace license has expired.", userId);
            sendPaymentRequiredResponse(response);
            return false;
        }

        return true;
    }

    /**
     * Extracts and validates the authenticated user UUID from {@link SecurityContextHolder}.
     *
     * <p>Example usage:
     * <pre>{@code
     * Optional<UUID> activeUserId = resolveCurrentUserId();
     * }</pre>
     *
     * @return an {@link Optional} containing the user {@link UUID}, or empty if unauthenticated or anonymous
     */
    private Optional<UUID> resolveCurrentUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || ANONYMOUS_PRINCIPAL.equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(auth.getName()));
        } catch (IllegalArgumentException ex) {
            log.warn("Authentication principal name '{}' is not a valid UUID format.", auth.getName());
            return Optional.empty();
        }
    }

    /**
     * Serializes and writes a standardized {@link ErrorResponseDTO} with HTTP 402 status to the response output stream.
     *
     * <p>Example usage:
     * <pre>{@code
     * sendPaymentRequiredResponse(response);
     * }</pre>
     *
     * @param response the current {@link HttpServletResponse}
     * @throws IOException if writing JSON characters to the response writer fails
     */
    private void sendPaymentRequiredResponse(final HttpServletResponse response) throws IOException {
        final HttpStatus status = HttpStatus.PAYMENT_REQUIRED;
        final String correlationId = MDC.get(MDC_CORRELATION_ID);

        final ErrorResponseDTO errorPayload =
            new ErrorResponseDTO(status.value(), status.getReasonPhrase(), LICENSE_EXPIRED_MESSAGE, Instant.now(),
                correlationId);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(errorPayload));
    }
}