package de.haevn.worksuite.about;

import java.time.Instant;
import java.util.Map;

public record AboutSystemInfoResponse(
    String appName,
    String version,
    String gitCommit,
    String environment,
    Instant buildTimestamp,
    Instant serverTime,
    String javaVersion,
    String springBootVersion,
    String osName,
    String osArch,
    long uptimeSeconds,
    Map<String, String> serviceHealth,
    DatabaseInfo postgresInfo,
    DatabaseInfo mongoInfo
) {}