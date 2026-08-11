package de.haevn.worksuite.database;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Log4j2
@RequiredArgsConstructor
@Component
public class DatabaseRecordRepository {
    private final ObjectMapper objectMapper;

    @Cacheable("databaseRecords")
    public Map<String, List<DatabaseRecord>> getDatabaseRecords() {
        log.info("Fetching database records");
        try {
            final FileSystemResource resource = new FileSystemResource("/shared/records.json");
            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, new TypeReference<Map<String, List<DatabaseRecord>>>() {
                });
            }
        } catch (Exception e) {
            log.error("Failed to load database records", e);
            return Collections.emptyMap();
        }
    }
}
