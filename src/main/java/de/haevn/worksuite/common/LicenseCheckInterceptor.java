package de.haevn.worksuite.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.haevn.worksuite.settings.LicenseService;
import de.haevn.worksuite.settings.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseCheckInterceptor implements HandlerInterceptor {

    private final LicenseService licenseService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        final UUID userId = resolveCurrentUserId();
        if (userId == null) {
            return true;
        }

        if (licenseService.licenseExpired(userId)) {
            log.warn("Access denied for user {}: License is expired", userId);
            sendPaymentRequiredResponse(response);
            return false;
        }

        return true;
    }

    private UUID resolveCurrentUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            log.warn("Authentication name '{}' is not a valid UUID", auth.getName());
            return null;
        }
    }

    private void sendPaymentRequiredResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.PAYMENT_REQUIRED.value()); // HTTP 402
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        final Map<String, Object> errorPayload = Map.of(
            "status", HttpStatus.PAYMENT_REQUIRED.value(),
            "error", "Payment Required",
            "message", "Your workspace license has expired. Please renew your subscription to continue."
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorPayload));
    }
}