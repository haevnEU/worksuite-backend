package de.haevn.worksuite.about;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data transfer record providing health, driver details, and connection pool statistics
 * for a database instance.
 *
 * <p>Example usage:
 * <pre>{@code
 * DatabaseInfo info = DatabaseInfo.offline("PostgreSQL");
 * }</pre>
 *
 * @param databaseProductName the commercial name of the database engine
 * @param databaseProductVersion the release version of the running database server
 * @param driverName the name and version of the active client driver
 * @param url the connection URL or database name (credentials sanitized)
 * @param status the operational status (e.g. {@code UP}, {@code DOWN})
 * @param pingMs round-trip ping time in milliseconds, or -1 if unreachable
 * @param activeConnections number of active pooled connections
 * @param idleConnections number of idle pooled connections
 * @param totalConnections total number of pooled connections
 */
@Schema(description = "Detailed health, version, and connection pool metrics for a database instance")
public record DatabaseInfo(

    @Schema(description = "Commercial database product name", example = "PostgreSQL") String databaseProductName,

    @Schema(description = "Database product release version", example = "16.15") String databaseProductVersion,

    @Schema(description = "Client driver identifier and version",
        example = "PostgreSQL JDBC Driver 42.7.3") String driverName,

    @Schema(description = "Sanitized connection URL or database identifier",
        example = "jdbc:postgresql://postgres:5432/worksuite") String url,

    @Schema(description = "Operational health status", example = "UP",
        allowableValues = {"UP", "DOWN", "UNKNOWN"}) String status,

    @Schema(description = "Ping response latency in milliseconds (-1 if offline)", example = "3") long pingMs,

    @Schema(description = "Current number of active connections in use", example = "2") int activeConnections,

    @Schema(description = "Current number of idle pooled connections", example = "8") int idleConnections,

    @Schema(description = "Total pooled connections allocated", example = "10") int totalConnections) {

    /**
     * Factory method creating a standardized offline representation for an unreachable database.
     *
     * @param productName the name of the database system (e.g. {@code "PostgreSQL"}, {@code "MongoDB"})
     * @return a {@link DatabaseInfo} configured with fallback error values
     */
    public static DatabaseInfo offline(final String productName) {
        return new DatabaseInfo(productName, "Unknown", "Unknown", "N/A", "DOWN", -1L, 0, 0, 0);
    }

    /**
     * Factory method creating an offline indicator when the database identity is unknown.
     *
     * @return a {@link DatabaseInfo} representing an unidentifiable offline database
     */
    public static DatabaseInfo unknown() {
        return offline("Unknown");
    }
}