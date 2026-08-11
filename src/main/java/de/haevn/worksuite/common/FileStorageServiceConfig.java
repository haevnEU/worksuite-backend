package de.haevn.worksuite.common;

import java.util.Optional;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FileStorageServiceConfig {

    @Bean
    @Scope("prototype")
    public FileStorageService fileIO(final InjectionPoint injectionPoint) {
        final String targetClassName =
            Optional.of(injectionPoint.getMember()).map(member -> member.getDeclaringClass().getSimpleName())
                .orElse("unknown");
        return new FileStorageService(targetClassName);
    }
}