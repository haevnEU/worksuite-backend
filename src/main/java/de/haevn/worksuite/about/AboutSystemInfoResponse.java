package de.haevn.worksuite.about;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

/**
 * Root response DTO aggregating application metadata, build information, runtime environment,
 * sub-service health, and database connection metrics.
 *
 * <p>Example serialization:
 * <pre>{@code
 * {
 *   "appName": "Worksuite Core",
 *   "version": "1.0.0",
 *   "gitCommit": "a1b2c3d",
 *   "environment": "production",
 *   "uptimeSeconds": 1420
 * }
 * }</pre>
 *
 * @param appName the registered application name
 * @param version the current build version identifier
 * @param gitCommit the short commit hash the application was compiled against
 * @param environment active Spring configuration profiles (comma-separated)
 * @param buildTimestamp compilation timestamp or JVM start time fallback
 * @param serverTime current UTC time on the application host
 * @param javaVersion the Java runtime version and vendor details
 * @param springBootVersion the version of Spring Boot running the application
 * @param osName host operating system name
 * @param osArch host operating system CPU architecture
 * @param uptimeSeconds total elapsed runtime of the JVM in seconds
 * @param serviceHealth map containing health indicators of internal subsystems
 * @param postgresInfo detailed {@link DatabaseInfo} for relational PostgreSQL storage
 * @param mongoInfo detailed {@link DatabaseInfo} for document MongoDB storage
 */
@Schema(description = "Aggregated system status, build metadata, and infrastructure health report")
public record AboutSystemInfoResponse(

    @Schema(description = "Application name", example = "Worksuite Core") String appName,

    @Schema(description = "Application build version", example = "1.0.0-SNAPSHOT") String version,

    @Schema(description = "Git short commit hash", example = "7a8b9c0") String gitCommit,

    @Schema(description = "Active deployment profiles", example = "prod, docker") String environment,

    @Schema(description = "Application artifact build timestamp") Instant buildTimestamp,

    @Schema(description = "Current server clock timestamp in UTC") Instant serverTime,

    @Schema(description = "Host Java runtime version and vendor", example = "25.0.3 (Homebrew)") String javaVersion,

    @Schema(description = "Underlying Spring Boot platform version", example = "4.1.0") String springBootVersion,

    @Schema(description = "Host operating system name", example = "Linux") String osName,

    @Schema(description = "Host CPU architecture", example = "amd64") String osArch,

    @Schema(description = "Total JVM process uptime in seconds", example = "3600") long uptimeSeconds,

    @Schema(
        description = "Key-value mapping of internal service and gateway health statuses") Map<String, String> serviceHealth,

    @Schema(description = "Metrics and connection pool state for the PostgreSQL database") DatabaseInfo postgresInfo,

    @Schema(description = "Metrics and ping status for the MongoDB database") DatabaseInfo mongoInfo) {
}