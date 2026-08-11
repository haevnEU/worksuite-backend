package de.haevn.worksuite.config;

import de.haevn.worksuite.common.FileStorageService;
import java.lang.reflect.Member;
import java.util.Optional;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Spring configuration creating module-aware prototype beans of {@link FileStorageService}.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Service
 * public class OrderAttachmentService {
 *     // Injected FileStorageService automatically isolates files to the "OrderAttachmentService" directory
 *     private final FileStorageService fileStorageService;
 *
 *     public OrderAttachmentService(FileStorageService fileStorageService) {
 *         this.fileStorageService = fileStorageService;
 *     }
 * }
 * }</pre>
 */
@Configuration
public class FileStorageServiceConfig {

    private static final String DEFAULT_MODULE_NAME = "common";

    /**
     * Creates a new prototype instance of {@link FileStorageService} parameterized with the declaring class name
     * of the injection target.
     *
     * @param injectionPoint metadata describing the target field, method, or constructor parameter receiving injection
     * @return a configured {@link FileStorageService} scoped to the declaring class module
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FileStorageService fileStorageService(final InjectionPoint injectionPoint) {
        final String moduleName = resolveTargetModuleName(injectionPoint);
        return new FileStorageService(moduleName);
    }

    /**
     * Resolves the target module directory name from an {@link InjectionPoint}.
     *
     * <p>Example:
     * <pre>{@code
     * String module = resolveTargetModuleName(injectionPoint);
     * }</pre>
     *
     * @param injectionPoint the Spring injection point to inspect
     * @return the simple name of the declaring class, or {@code "common"} if resolution fails
     */
    private String resolveTargetModuleName(final InjectionPoint injectionPoint) {
        return Optional.ofNullable(injectionPoint.getMember()).map(Member::getDeclaringClass).map(Class::getSimpleName)
            .orElse(DEFAULT_MODULE_NAME);
    }
}