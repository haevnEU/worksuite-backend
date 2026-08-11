package de.haevn.worksuite.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

/**
 * Repository responsible for loading and caching table records from the shared JSON store.
 *
 * <p>Results are cached using Spring's {@link Cacheable} abstraction under the {@code databaseRecords}
 * cache namespace.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private DatabaseRecordRepository repository;
 *
 * Map<String, List<DatabaseRecord>> recordsByTable = repository.getDatabaseRecords();
 * List<DatabaseRecord> userRoles = recordsByTable.get("user_roles");
 * }</pre>
 */
@Log4j2
@Repository
@RequiredArgsConstructor
public class DatabaseRecordRepository {

    private static final String RECORDS_FILE_PATH = "/shared/records.json";
    private static final String CACHE_NAME = "databaseRecords";

    private final ObjectMapper objectMapper;

    /**
     * Retrieves all database records mapped by table name from the underlying storage or cache.
     *
     * @return an unmodifiable or deserialized {@link Map} containing lists of {@link DatabaseRecord} per table name
     */
    @Cacheable(CACHE_NAME)
    public Map<String, List<DatabaseRecord>> getDatabaseRecords() {
        log.info("Loading database records from persistent file: {}", RECORDS_FILE_PATH);
        final Resource resource = new FileSystemResource(Path.of(RECORDS_FILE_PATH));

        if (!resource.exists() || !resource.isReadable()) {
            log.warn("Database records file does not exist or is not readable: {}", RECORDS_FILE_PATH);
            return Collections.emptyMap();
        }

        try (final InputStream inputStream = resource.getInputStream()) {
            return readRecordsStream(inputStream);
        } catch (Exception ex) {
            log.error("Failed to read and parse database records from '{}'", RECORDS_FILE_PATH, ex);
            return Collections.emptyMap();
        }
    }

    /**
     * Deserializes JSON input stream content into a table-mapped {@link Map} structure.
     *
     * <p>Example usage:
     * <pre>{@code
     * try (InputStream is = Files.newInputStream(path)) {
     *     Map<String, List<DatabaseRecord>> data = readRecordsStream(is);
     * }
     * }</pre>
     *
     * @param inputStream binary JSON stream to read
     * @return deserialized map of database records
     * @throws IOException if deserialization fails
     */
    private Map<String, List<DatabaseRecord>> readRecordsStream(final InputStream inputStream) throws IOException {
        final TypeReference<Map<String, List<DatabaseRecord>>> typeRef = new TypeReference<>() {
        };
        final Map<String, List<DatabaseRecord>> parsed = objectMapper.readValue(inputStream, typeRef);
        return parsed != null ? parsed : Collections.emptyMap();
    }
}