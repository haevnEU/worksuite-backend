package de.haevn.worksuite.about;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AboutService {

    private final ObjectProvider<BuildProperties> buildPropertiesProvider;
    private final ObjectProvider<GitProperties> gitPropertiesProvider;
    private final Environment environment;

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

        return new AboutSystemInfoResponse(appName, appVersion, gitCommit, activeProfile, buildTimestamp, Instant.now(),
            javaVersion, springBootVersion, osName, osArch, uptimeSeconds,
            Map.of("database", "UP", "vcsGateway", "UP", "redmineGateway", "UP"));
    }
}