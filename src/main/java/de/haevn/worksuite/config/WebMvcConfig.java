package de.haevn.worksuite.config;

import de.haevn.worksuite.config.interceptor.LicenseCheckInterceptor;
import de.haevn.worksuite.config.interceptor.RouteUsageTrackingInterceptor;
import de.haevn.worksuite.config.interceptor.UserIntegrationContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Consolidated Spring Web MVC configuration managing interceptor registration and CORS policies.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/ws/**",
        "/api/v1/share",
        "/api/v1/share/**",
        "/api/v1/about",
        "/api/v1/about/**",
        "/actuator/health"
    };

    private static final String[] SWAGGER_EXCLUSIONS = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    };

    private static final String[] ALLOWED_ORIGIN_PATTERNS = {
        "http://localhost:*",
        "http://127.0.0.1:*"
    };

    private final LicenseCheckInterceptor licenseCheckInterceptor;
    private final UserIntegrationContextInterceptor userIntegrationContextInterceptor;
    private final RouteUsageTrackingInterceptor routeUsageTrackingInterceptor;

    /**
     * Configures Spring MVC interceptors.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull final InterceptorRegistry registry) {
        registry.addInterceptor(userIntegrationContextInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(PUBLIC_ENDPOINTS)
            .excludePathPatterns(SWAGGER_EXCLUSIONS);

        registry.addInterceptor(licenseCheckInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(PUBLIC_ENDPOINTS)
            .excludePathPatterns(SWAGGER_EXCLUSIONS);

        registry.addInterceptor(routeUsageTrackingInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(SWAGGER_EXCLUSIONS);
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) mappings.
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(@NonNull final CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS)
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
            )
            .allowedHeaders("*")
            .exposedHeaders(
                HttpHeaders.CONTENT_DISPOSITION,
                HttpHeaders.CONTENT_LENGTH,
                "X-Correlation-ID"
            )
            .allowCredentials(true)
            .maxAge(3600);
    }
}