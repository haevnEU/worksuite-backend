package de.haevn.worksuite.config.metrics;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Startup component that extracts all registered Spring MVC endpoints and initializes the PostgreSQL route catalog.
 *
 * <p>Listens for {@link ApplicationReadyEvent} to discover mapping metadata from {@link RequestMappingHandlerMapping}
 * and inserts every route pattern with an initial invocation count of {@code 0}.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Automatically triggered on application startup:
 * // Discovers endpoint POST /api/v1/notes and seeds:
 * // (NoteController, createNote, POST, /api/v1/notes, 0, now)
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteRegistryInitializer {

    private static final String PARAM_CONTROLLER_CLASS = "controllerClass";
    private static final String PARAM_CONTROLLER_METHOD = "controllerMethod";
    private static final String PARAM_HTTP_METHOD = "httpMethod";
    private static final String PARAM_PATTERN = "pattern";

    private static final String SEED_SQL = """
        INSERT INTO route_usage_metrics 
            (controller_class, controller_method, http_method, pattern, invocation_count, first_seen_at)
        VALUES 
            (:controllerClass, :controllerMethod, :httpMethod, :pattern, 0, CURRENT_TIMESTAMP)
        ON CONFLICT (http_method, pattern) DO NOTHING;
        """;

    private final RequestMappingHandlerMapping handlerMapping;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Inspects Spring MVC endpoint mappings on application startup and persists all discovered routes into the database.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void seedAllRoutes() {
        final Map<RequestMappingInfo, HandlerMethod> endpoints = handlerMapping.getHandlerMethods();

        endpoints.forEach((mappingInfo, handlerMethod) -> {
            final String controllerClass = handlerMethod.getBeanType().getSimpleName();
            final String controllerMethod = handlerMethod.getMethod().getName();

            mappingInfo.getDirectPaths().forEach(path ->
                insertRoute(controllerClass, controllerMethod, mappingInfo, path)
            );

            if (mappingInfo.getPathPatternsCondition() != null) {
                mappingInfo.getPathPatternsCondition().getPatterns().forEach(pattern ->
                    insertRoute(controllerClass, controllerMethod, mappingInfo, pattern.getPatternString())
                );
            }
        });

        log.info("Initialized static API route catalog in PostgreSQL with {} mapped endpoints.", endpoints.size());
    }

    /**
     * Inserts an individual route endpoint mapping into the database for each configured HTTP method.
     *
     * <p>Example usage:
     * <pre>{@code
     * insertRoute("NoteController", "getNoteById", mappingInfo, "/api/v1/notes/{id}");
     * }</pre>
     *
     * @param controllerClass the simple class name of the declaring REST controller
     * @param controllerMethod the name of the handler method handling the request
     * @param info the {@link RequestMappingInfo} containing HTTP method constraints
     * @param pattern the route URL path pattern string
     */
    private void insertRoute(
        final String controllerClass,
        final String controllerMethod,
        final RequestMappingInfo info,
        final String pattern
    ) {
        Objects.requireNonNull(info, "RequestMappingInfo must not be null");

        info.getMethodsCondition().getMethods().forEach(requestMethod -> {
            final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PARAM_CONTROLLER_CLASS, controllerClass)
                .addValue(PARAM_CONTROLLER_METHOD, controllerMethod)
                .addValue(PARAM_HTTP_METHOD, requestMethod.name())
                .addValue(PARAM_PATTERN, pattern);

            jdbcTemplate.update(SEED_SQL, params);
        });
    }
}