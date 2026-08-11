package de.haevn.worksuite.about;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * Service collecting environmental metadata, JVM runtime metrics, build signatures,
 * and asynchronous health diagnostics across data stores.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private AboutService aboutService;
 *
 * AboutSystemInfoResponse status = aboutService.getSystemInfo();
 * }</pre>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AboutService {

    private final ObjectProvider<BuildProperties> buildPropertiesProvider;
    private final ObjectProvider<GitProperties> gitPropertiesProvider;
    private final Environment environment;
    private final ObjectProvider<DataSource> dataSourceProvider;
    private final ObjectProvider<MongoTemplate> mongoTemplateProvider;

    /**
     * Compiles an aggregated {@link AboutSystemInfoResponse} describing the current application instance.
     *
     * @return the assembled {@link AboutSystemInfoResponse} report
     */
    public AboutSystemInfoResponse getSystemInfo() {
        final BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
        final GitProperties gitProperties = gitPropertiesProvider.getIfAvailable();

        final String appName = buildProperties != null ? buildProperties.getName() : "Worksuite Core";
        final String appVersion = buildProperties != null ? buildProperties.getVersion() : "dev-SNAPSHOT";
        final Instant buildTimestamp = buildProperties != null ?
            buildProperties.getTime() :
            Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());

        final String gitCommit = gitProperties != null ? gitProperties.getShortCommitId() : "local-dev";
        final String[] activeProfiles = environment.getActiveProfiles();
        final String activeProfile = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";

        final long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        final String javaVersion =
            System.getProperty("java.version", "unknown") + " (" + System.getProperty("java.vendor", "OpenJDK") + ")";
        final String springBootVersion =
            SpringBootVersion.getVersion() != null ? SpringBootVersion.getVersion() : "unknown";
        final String osName = System.getProperty("os.name", "Linux");
        final String osArch = System.getProperty("os.arch", "amd64");

        final DatabaseInfo postgresInfo = collectPostgresInfo();
        final DatabaseInfo mongoInfo = collectMongoInfo();

        final Map<String, String> serviceHealth =
            Map.of("database", postgresInfo != null && "UP".equals(postgresInfo.status()) ? "UP" : "DOWN", "mongo",
                mongoInfo != null && "UP".equals(mongoInfo.status()) ? "UP" : "DOWN", "vcsGateway", "UP",
                "redmineGateway", "UP");

        return new AboutSystemInfoResponse(appName, appVersion, gitCommit, activeProfile, buildTimestamp, Instant.now(),
            javaVersion, springBootVersion, osName, osArch, uptimeSeconds, serviceHealth, postgresInfo, mongoInfo);
    }

    /**
     * Executes an isolated connection validation and extracts pool statistics from the configured {@link DataSource}.
     *
     * @return the populated {@link DatabaseInfo}, or {@code null} if no data source is configured
     */
    private DatabaseInfo collectPostgresInfo() {
        final DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return null;
        }

        final long start = System.currentTimeMillis();

        try {
            // Asynchronously validate connection with a timeout to avoid blocking caller threads on DB hang
            return CompletableFuture.supplyAsync(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    final boolean isValid = connection.isValid(1);
                    final long pingMs = System.currentTimeMillis() - start;

                    if (!isValid) {
                        return DatabaseInfo.offline("PostgreSQL");
                    }

                    final DatabaseMetaData metaData = connection.getMetaData();

                    int active = 0;
                    int idle = 0;
                    int total = 0;
                    // Extract connection pool metrics if HikariCP is utilized
                    if (dataSource instanceof HikariDataSource hikariDs) {
                        final HikariPoolMXBean poolBean = hikariDs.getHikariPoolMXBean();
                        if (poolBean != null) {
                            active = poolBean.getActiveConnections();
                            idle = poolBean.getIdleConnections();
                            total = poolBean.getTotalConnections();
                        }
                    }

                    final String rawUrl = metaData.getURL();
                    // Sanitize sensitive credentials from database connection string before exposure
                    final String cleanUrl =
                        rawUrl != null ? rawUrl.replaceAll("(?i)(password=)[^;&]*", "$1*****") : "N/A";

                    return new DatabaseInfo(metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion(),
                        metaData.getDriverName() + " " + metaData.getDriverVersion(), cleanUrl, "UP", pingMs, active,
                        idle, total);
                } catch (Exception e) {
                    log.warn("PostgreSQL connection check failed: {}", e.getMessage());
                    return DatabaseInfo.offline("PostgreSQL");
                }
            }).get(1200, TimeUnit.MILLISECONDS);

        } catch (TimeoutException te) {
            log.warn("PostgreSQL health check timed out (>1200ms) - database unreachable");
            return DatabaseInfo.offline("PostgreSQL");
        } catch (Exception e) {
            log.warn("PostgreSQL is unreachable: {}", e.getMessage());
            return DatabaseInfo.offline("PostgreSQL");
        }
    }

    /**
     * Executes a ping command and retrieves build metadata from the active {@link MongoTemplate}.
     *
     * @return the populated {@link DatabaseInfo}, or {@code null} if MongoDB is not configured
     */
    private DatabaseInfo collectMongoInfo() {
        final MongoTemplate mongoTemplate = mongoTemplateProvider.getIfAvailable();
        if (mongoTemplate == null) {
            return null;
        }

        final long start = System.currentTimeMillis();

        try {
            // Asynchronously ping MongoDB cluster with a timeout guard
            return CompletableFuture.supplyAsync(() -> {
                final Document pingCmd = new Document("ping", 1).append("maxTimeMS", 1000);
                final Document pingResult = mongoTemplate.executeCommand(pingCmd);

                final long pingMs = System.currentTimeMillis() - start;

                final Document buildInfoCmd = new Document("buildInfo", 1).append("maxTimeMS", 1000);
                final Document buildInfo = mongoTemplate.executeCommand(buildInfoCmd);
                final String version = buildInfo.getString("version");

                return new DatabaseInfo("MongoDB", version != null ? version : "Unknown", "MongoDB Java Sync Driver",
                    mongoTemplate.getDb().getName(), pingResult.containsKey("ok") ? "UP" : "DOWN", pingMs, 0, 0, 0);
            }).get(1200, TimeUnit.MILLISECONDS);

        } catch (TimeoutException te) {
            log.warn("MongoDB health check timed out (>1200ms) - server unreachable");
            return DatabaseInfo.offline("MongoDB");
        } catch (Exception e) {
            log.warn("MongoDB is unreachable: {}", e.getMessage());
            return DatabaseInfo.offline("MongoDB");
        }
    }
}