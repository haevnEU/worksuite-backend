package de.haevn.worksuite.config.interceptor;

import de.haevn.worksuite.config.UserContextHolder;
import de.haevn.worksuite.config.UserIntegrationContext;
import de.haevn.worksuite.settings.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserIntegrationContextInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
        final Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                final UUID userId = UUID.fromString(auth.getName());
                final var user = userService.getUser(userId);

                UserContextHolder.setContext(
                    new UserIntegrationContext(user.getId(), user.getRedmineKey(), user.getVcsKey()));
            } catch (IllegalArgumentException e) {
                log.warn("Cannot resolve userId UUID from principal: {}", auth.getName());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
        final Object handler, @Nullable Exception ex) {
        UserContextHolder.clear();
    }
}