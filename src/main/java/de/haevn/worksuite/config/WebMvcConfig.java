package de.haevn.worksuite.config;

import de.haevn.worksuite.config.interceptor.LicenseCheckInterceptor;
import de.haevn.worksuite.config.interceptor.UserIntegrationContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LicenseCheckInterceptor licenseCheckInterceptor;
    private final UserIntegrationContextInterceptor userIntegrationContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userIntegrationContextInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns("/api/v1/about/**", "/api/v1/share/**");

        registry.addInterceptor(licenseCheckInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns("/api/v1/about/**", "/api/v1/share/**");
    }
}