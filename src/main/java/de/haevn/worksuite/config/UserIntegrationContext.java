package de.haevn.worksuite.config;

import java.util.UUID;

public record UserIntegrationContext(
    UUID userId,
    String redmineApiKey,
    String vcsToken
) {}