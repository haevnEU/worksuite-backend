package de.haevn.worksuite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration establishing global Cross-Origin Resource Sharing (CORS) rules.
 *
 * <p>Enables localhost and local loopback origins for local frontend development environments while exposing
 * necessary content headers and supporting credentials.
 *
 * <p>Example applied configuration:
 * <pre>{@code
 * // Routes: /api/**
 * // Allowed Origins: http://localhost:*, http://127.0.0.1:*
 * // Allowed Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
 * // Exposed Headers: Content-Disposition, Content-Length
 * }</pre>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String API_PATH_PATTERN = "/api/**";
    private static final String[] ALLOWED_ORIGIN_PATTERNS = {"http://localhost:*", "http://127.0.0.1:*"};
    private static final long CORS_MAX_AGE_SECONDS = 3600L;

    /**
     * Configures CORS mappings for incoming API requests across defined development origin patterns.
     *
     * @param registry the {@link CorsRegistry} to attach configurations to
     */
    @Override
    public void addCorsMappings(@NonNull final CorsRegistry registry) {
        registry.addMapping(API_PATH_PATTERN).allowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS)
            .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()).allowedHeaders("*")
            .exposedHeaders(HttpHeaders.CONTENT_DISPOSITION, HttpHeaders.CONTENT_LENGTH).allowCredentials(true)
            .maxAge(CORS_MAX_AGE_SECONDS);
    }
}