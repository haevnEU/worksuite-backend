package de.haevn.worksuite.config.interceptor;

import de.haevn.worksuite.config.UserContextHolder;
import de.haevn.worksuite.config.UserIntegrationContext;
import de.haevn.worksuite.settings.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that loads user integration metadata into {@link UserContextHolder} for authenticated requests.
 *
 * <p>Example execution:
 * <pre>{@code
 * // Request executes -> preHandle resolves User entity and populates UserContextHolder
 * // Controller executes -> UserContextHolder.getContext() yields integration keys
 * // Request completes -> afterCompletion cleans up UserContextHolder
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserIntegrationContextInterceptor implements HandlerInterceptor {

    private static final String ANONYMOUS_PRINCIPAL = "anonymousUser";
    private final UserService userService;

    /**
     * Resolves authenticated user integration keys before handler execution.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @param handler chosen handler to execute
     * @return {@code true} to continue the execution chain
     */
    @Override
    public boolean preHandle(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
        @NonNull final Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAuthenticatedUser(auth)) {
            populateUserContext(auth.getName());
        }
        return true;
    }

    /**
     * Clears {@link UserContextHolder} after request completion.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @param handler chosen handler to execute
     * @param ex any exception thrown on handler execution
     */
    @Override
    public void afterCompletion(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
        @NonNull final Object handler, final Exception ex) {
        UserContextHolder.clear();
    }

    /**
     * Verifies that the {@link Authentication} token represents a verified, non-anonymous user.
     *
     * <p>Example:
     * <pre>{@code
     * boolean valid = isAuthenticatedUser(auth);
     * }</pre>
     *
     * @param auth current Spring Security authentication object
     * @return {@code true} if authenticated as a registered user
     */
    private boolean isAuthenticatedUser(final Authentication auth) {
        return auth != null && auth.isAuthenticated() && !ANONYMOUS_PRINCIPAL.equals(auth.getPrincipal());
    }

    /**
     * Fetches user data via {@link UserService} and initializes the {@link UserIntegrationContext}.
     *
     * <p>Example:
     * <pre>{@code
     * populateUserContext("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
     * }</pre>
     *
     * @param principalName the subject identifier (UUID string)
     */
    private void populateUserContext(final String principalName) {
        try {
            final UUID userId = UUID.fromString(principalName);
            final var user = userService.getUser(userId);

            UserContextHolder.setContext(
                new UserIntegrationContext(user.getId(), user.getRedmineKey(), user.getVcsKey()));
        } catch (IllegalArgumentException ex) {
            log.warn("Cannot resolve valid UUID from authentication principal: {}", principalName);
        } catch (Exception ex) {
            log.error("Failed to load user integration context for user ID: {}", principalName, ex);
        }
    }
}