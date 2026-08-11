package de.haevn.worksuite.about;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing persistence and analytical queries for {@link RouteUsageMetric} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private RouteUsageMetricRepository repository;
 *
 * List<RouteUsageMetric> unusedRoutes = repository.findUnusedRoutes();
 * }</pre>
 */
@Repository
public interface RouteUsageMetricRepository extends JpaRepository<RouteUsageMetric, UUID> {

    /**
     * Finds a route metric entry by its HTTP method and path pattern.
     *
     * @param httpMethod HTTP method name (e.g., "GET")
     * @param pattern URL path pattern (e.g., "/api/v1/notes/{id}")
     * @return an {@link Optional} holding the matched metric entry
     */
    Optional<RouteUsageMetric> findByHttpMethodAndPattern(String httpMethod, String pattern);

    /**
     * Retrieves all routes that have never been invoked (Dead Code Analysis).
     *
     * @return list of unused {@link RouteUsageMetric} entities
     */
    @Query("SELECT r FROM RouteUsageMetric r WHERE r.invocationCount = 0 ORDER BY r.controllerClass ASC, r.pattern ASC")
    List<RouteUsageMetric> findUnusedRoutes();

    /**
     * Retrieves all routes ordered by access frequency descending.
     *
     * @return list of {@link RouteUsageMetric} entities sorted by highest invocation count
     */
    @Query("SELECT r FROM RouteUsageMetric r ORDER BY r.invocationCount DESC")
    List<RouteUsageMetric> findMostUsedRoutes();
}