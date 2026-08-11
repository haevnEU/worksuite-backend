package de.haevn.worksuite.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Service managing query operations, filtering, and table discovery for {@link DatabaseRecord} datasets.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private DatabaseService databaseService;
 *
 * // Find across all tables by key:
 * Map<String, List<DatabaseRecord>> results = databaseService.getDatabaseRecords(
 *     Optional.of("key"),
 *     Optional.of("AUTH_ADMIN")
 * );
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DatabaseService {

    private static final String PARAM_ID = "id";
    private static final String PARAM_KEY = "key";

    private final DatabaseRecordRepository databaseRecordRepository;

    /**
     * Retrieves records across all tables, optionally applying a field-level filter.
     *
     * @param searchParam optional filter field (e.g., {@code "id"} or {@code "key"})
     * @param value optional match value
     * @return a map of table names to matching lists of {@link DatabaseRecord}
     */
    public Map<String, List<DatabaseRecord>> getDatabaseRecords(final Optional<String> searchParam,
        final Optional<String> value) {
        if (searchParam.isPresent() && value.isPresent()) {
            final String param = searchParam.get().trim();
            final String val = value.get().trim();

            return switch (param.toLowerCase()) {
                case PARAM_ID -> findById(val);
                case PARAM_KEY -> findByKey(val);
                default -> {
                    log.warn("Unsupported search parameter provided: '{}'", param);
                    yield Collections.emptyMap();
                }
            };
        }
        return databaseRecordRepository.getDatabaseRecords();
    }

    /**
     * Retrieves records for a specific table, optionally applying a field-level filter.
     *
     * @param table the target table identifier
     * @param searchParam optional filter field (e.g., {@code "id"} or {@code "key"})
     * @param value optional match value
     * @return a list of matching {@link DatabaseRecord} objects within the specified table
     */
    public List<DatabaseRecord> getDatabaseRecords(final String table, final Optional<String> searchParam,
        final Optional<String> value) {
        final List<DatabaseRecord> targetTable =
            databaseRecordRepository.getDatabaseRecords().getOrDefault(table, Collections.emptyList());

        if (searchParam.isPresent() && value.isPresent()) {
            final String param = searchParam.get().trim();
            final String val = value.get().trim();

            final Optional<Predicate<DatabaseRecord>> filterPredicate = buildFilterPredicate(param, val);
            if (filterPredicate.isEmpty()) {
                log.warn("Unsupported search parameter '{}' for table '{}'", param, table);
                return Collections.emptyList();
            }

            return targetTable.stream().filter(filterPredicate.get()).toList();
        }

        return targetTable;
    }

    /**
     * Retrieves all available table names.
     *
     * @return a sorted list of table name strings
     */
    public List<String> getTables() {
        return databaseRecordRepository.getDatabaseRecords().keySet().stream().sorted().toList();
    }

    /**
     * Filters all tables for entries matching the specified numerical ID.
     *
     * @param idString numerical ID as a string
     * @return map of matching records grouped by table
     */
    public Map<String, List<DatabaseRecord>> findById(final String idString) {
        try {
            final long searchId = Long.parseLong(idString);
            return filterRecords(record -> record.id() == searchId);
        } catch (NumberFormatException ex) {
            log.warn("Invalid numerical ID format provided for search: '{}'", idString);
            return Collections.emptyMap();
        }
    }

    /**
     * Filters all tables for entries matching the exact business key.
     *
     * @param key business key to match
     * @return map of matching records grouped by table
     */
    public Map<String, List<DatabaseRecord>> findByKey(final String key) {
        return filterRecords(record -> record.key().equals(key));
    }

    /**
     * Evaluates a predicate against all records in every table, omitting empty resulting tables.
     *
     * <p>Example usage:
     * <pre>{@code
     * Map<String, List<DatabaseRecord>> matching = filterRecords(r -> r.id() > 500);
     * }</pre>
     *
     * @param predicate the matching filter condition
     * @return filtered map of tables to matching records
     */
    private Map<String, List<DatabaseRecord>> filterRecords(final Predicate<DatabaseRecord> predicate) {
        return databaseRecordRepository.getDatabaseRecords().entrySet().stream().map(entry -> {
                final List<DatabaseRecord> matching = entry.getValue().stream().filter(predicate).toList();
                return Map.entry(entry.getKey(), matching);
            }).filter(entry -> !entry.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Constructs a {@link Predicate} based on the parameter name and string value.
     *
     * <p>Example usage:
     * <pre>{@code
     * Optional<Predicate<DatabaseRecord>> predicate = buildFilterPredicate("id", "42");
     * }</pre>
     *
     * @param param search field name
     * @param value target filter value
     * @return an {@link Optional} containing the filter predicate, or empty if invalid
     */
    private Optional<Predicate<DatabaseRecord>> buildFilterPredicate(final String param, final String value) {
        return switch (param.toLowerCase()) {
            case PARAM_ID -> {
                try {
                    final long searchId = Long.parseLong(value);
                    yield Optional.of(record -> record.id() == searchId);
                } catch (NumberFormatException ex) {
                    log.warn("Failed to parse filter id '{}' as a valid number.", value);
                    yield Optional.empty();
                }
            }
            case PARAM_KEY -> Optional.of(record -> record.key().equals(value));
            default -> Optional.empty();
        };
    }
}