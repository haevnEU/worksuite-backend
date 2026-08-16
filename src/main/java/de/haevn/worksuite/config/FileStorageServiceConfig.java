package de.haevn.worksuite.config;

import de.haevn.worksuite.common.FileStorageService;
import java.lang.reflect.Member;
import java.util.Optional;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FileStorageServiceConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FileStorageService fileStorageService(final InjectionPoint injectionPoint) {
        final String targetClassName = Optional.ofNullable(injectionPoint.getMember())
            .map(Member::getDeclaringClass)
            .map(Class::getSimpleName)
            .orElse("common");

        return new FileStorageService(targetClassName);
    }
}