package de.haevn.worksuite.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class DatabaseService {
    private final DatabaseRecordRepository databaseRecordRepository;

    public Map<String, List<DatabaseRecord>> getDatabaseRecords(final Optional<String> searchParam,
        final Optional<String> value) {
        if (searchParam.isPresent() && value.isPresent()) {
            final String param = searchParam.get();
            final String val = value.get();

            switch (param) {
                case "id" -> {
                    return findById(val);
                }
                case "key" -> {
                    return findByKey(val);
                }
                default -> {
                    log.warn("Unknown search parameter: {}", param);
                    return Map.of();
                }
            }
        }
        return databaseRecordRepository.getDatabaseRecords();
    }

    private Map<String, List<DatabaseRecord>> filter(final Predicate<DatabaseRecord> predicate) {
        return databaseRecordRepository.getDatabaseRecords().entrySet().stream().map(entry -> {
                final List<DatabaseRecord> matchingRecords = entry.getValue().stream().filter(predicate).toList();
                return Map.entry(entry.getKey(), matchingRecords);
            }).filter(entry -> !entry.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, List<DatabaseRecord>> findById(String id) {
        final long searchId = Long.parseLong(id);
        return filter(record -> record.id() == searchId);
    }

    public Map<String, List<DatabaseRecord>> findByKey(String id) {
        return filter(record -> record.key().equals(id));
    }

    public List<DatabaseRecord> getDatabaseRecords(final String table, final Optional<String> searchParam, final Optional<String> value) {
        final List<DatabaseRecord> targetTable = databaseRecordRepository.getDatabaseRecords().getOrDefault(table, List.of());
        if(searchParam.isPresent() && value.isPresent()) {
            final String param = searchParam.get();
            final String val = value.get();
            switch (param) {
                case "id" -> {
                    return targetTable.stream().filter(record -> record.id() == Long.parseLong(val)).toList();
                }
                case "key" -> {
                    return targetTable.stream().filter(record -> record.key().equals(val)).toList();
                }
                default -> {
                    log.warn("Unknown search parameter: {}", param);
                    return List.of();
                }
            }
        }
        return targetTable;
    }

    public List<String> getTables() {
        return new ArrayList<>(databaseRecordRepository.getDatabaseRecords().keySet());
    }
}
