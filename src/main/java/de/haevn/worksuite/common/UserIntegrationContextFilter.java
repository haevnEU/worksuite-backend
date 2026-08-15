package de.haevn.worksuite.common;

import de.haevn.worksuite.settings.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class UserIntegrationContextFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/about")
            || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
        throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                final String userId = auth.getName();
                final var user = userService.getUser(UUID.fromString(userId));

                UserContextHolder.setContext(new UserIntegrationContext(user.getId(), user.getRedmineKey(), user.getVcsKey()));
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}