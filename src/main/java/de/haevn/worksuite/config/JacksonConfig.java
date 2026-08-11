package de.haevn.worksuite.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring configuration providing the customized primary {@link ObjectMapper}.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private ObjectMapper objectMapper;
 *
 * String json = objectMapper.writeValueAsString(myDto);
 * }</pre>
 */
@Configuration
public class JacksonConfig {

    /**
     * Configures and provides the primary {@link ObjectMapper} bean.
     *
     * @return pre-configured {@link ObjectMapper} instance
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return configureObjectMapper(new ObjectMapper());
    }

    /**
     * Applies serialization and deserialization defaults to the given {@link ObjectMapper}.
     *
     * <p>Example:
     * <pre>{@code
     * ObjectMapper mapper = configureObjectMapper(new ObjectMapper());
     * }</pre>
     *
     * @param mapper the mapper instance to configure
     * @return the configured {@link ObjectMapper} instance
     */
    private ObjectMapper configureObjectMapper(final ObjectMapper mapper) {
        return mapper.registerModule(new JavaTimeModule()).findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}