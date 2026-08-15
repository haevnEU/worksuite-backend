package de.haevn.worksuite.about;

public record DatabaseInfo(
    String databaseProductName,
    String databaseProductVersion,
    String driverName,
    String url,
    String status,
    long pingMs,
    int activeConnections,
    int idleConnections,
    int totalConnections
) {
    public static DatabaseInfo offline(String productName) {
        return new DatabaseInfo(
            productName,
            "Unknown",
            "Unknown",
            "N/A",
            "DOWN",
            -1L,
            0,
            0,
            0
        );
    }

    public static DatabaseInfo unknown() {
        return offline("Unknown");
    }
}