package de.haevn.worksuite.database;

import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/database")
public class DatabaseController {
    private final DatabaseService databaseService;

    @GetMapping
    public Map<String, List<DatabaseRecord>> getDatabaseRecords(
        @RequestParam(required = false) Optional<String> searchParam,
        @RequestParam(required = false) Optional<String> value) {
        log.info("Fetching database records");
        return databaseService.getDatabaseRecords(searchParam, value);
    }

    @GetMapping("/tables")
    public List<String> getDatabaseRecords() {
        log.info("Fetching database tables");
        return databaseService.getTables();
    }

    @GetMapping("/tables/{table}")
    public List<DatabaseRecord> getDatabaseRecords(@PathVariable String table,
        @RequestParam(required = false) Optional<String> searchParam,
        @RequestParam(required = false) Optional<String> value) {
        log.info("Fetching database records for table {}", table);
        return databaseService.getDatabaseRecords(table, searchParam, value);
    }
}
