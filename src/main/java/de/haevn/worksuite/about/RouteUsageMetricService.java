package de.haevn.worksuite.about;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing query operations and metric retrieval for {@link RouteUsageMetric} entities.
 *
 * <p>Provides insights into API route usage patterns, helping identify dead/unused endpoints
 * as well as high-traffic routes.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private RouteUsageMetricService metricService;
 *
 * List<RouteUsageMetricDTO> deadRoutes = metricService.getUnusedRoutes();
 * List<RouteUsageMetricDTO> topRoutes = metricService.getMostUsedRoutes();
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteUsageMetricService {

    private final RouteUsageMetricRepository metricRepository;

    /**
     * Retrieves all cataloged route metrics.
     *
     * @return a list containing all {@link RouteUsageMetricDTO} records
     */
    public List<RouteUsageMetricDTO> getAllMetrics() {
        log.info("Fetching all API route usage metrics");
        return metricRepository.findAll().stream()
            .map(RouteUsageMetricDTO::fromModel)
            .toList();
    }

    /**
     * Retrieves all endpoints that have never been invoked (Dead Code Analysis).
     *
     * @return a list of unused {@link RouteUsageMetricDTO} records sorted by controller class and pattern
     */
    public List<RouteUsageMetricDTO> getUnusedRoutes() {
        log.info("Fetching unused API routes (invocation_count = 0)");
        return metricRepository.findUnusedRoutes().stream()
            .map(RouteUsageMetricDTO::fromModel)
            .toList();
    }

    /**
     * Retrieves all endpoints ordered by their invocation frequency in descending order.
     *
     * @return a list of {@link RouteUsageMetricDTO} records ordered by highest traffic
     */
    public List<RouteUsageMetricDTO> getMostUsedRoutes() {
        log.info("Fetching most used API routes ordered by invocation count descending");
        return metricRepository.findMostUsedRoutes().stream()
            .map(RouteUsageMetricDTO::fromModel)
            .toList();
    }

    /**
     * Resets the invocation count and last-invoked timestamp for a specific route pattern.
     *
     * @param httpMethod the HTTP request method
     * @param pattern the route URL path pattern
     * @return {@code true} if the target route metric was found and reset, {@code false} otherwise
     */
    @Transactional
    public boolean resetMetric(final String httpMethod, final String pattern) {
        Objects.requireNonNull(httpMethod, "HTTP method must not be null");
        Objects.requireNonNull(pattern, "Pattern must not be null");

        log.info("Resetting route usage metric for [{} {}]", httpMethod, pattern);
        return metricRepository.findByHttpMethodAndPattern(httpMethod.toUpperCase(), pattern)
            .map(this::resetMetricValues)
            .orElse(false);
    }

    /**
     * Helper method to reset invocation values on a persistent {@link RouteUsageMetric} entity.
     *
     * <p>Example usage:
     * <pre>{@code
     * boolean reset = resetMetricValues(metricEntity);
     * }</pre>
     *
     * @param metric the persistent {@link RouteUsageMetric} entity
     * @return always {@code true} upon successful modification and save
     */
    private boolean resetMetricValues(final RouteUsageMetric metric) {
        metric.setInvocationCount(0L);
        metric.setLastInvokedAt(null);
        metricRepository.save(metric);
        return true;
    }
}