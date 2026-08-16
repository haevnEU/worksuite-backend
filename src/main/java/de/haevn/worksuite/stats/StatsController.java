package de.haevn.worksuite.stats;

import de.haevn.worksuite.common.RestApiController;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/stats")
public class StatsController {
    private final StatsService statsService;

    @PostMapping
    public UUID createNewRecord(@RequestParam final Optional<Instant> date) {
        if (date.isPresent()) {
            return statsService.createNewRecord(date.get());
        } else {
            return statsService.createNewRecord();
        }
    }

    @GetMapping("/{id}")
    public Stats getRecord(@PathVariable final UUID id) {
        return statsService.findStatsModelById(id);
    }

    @GetMapping
    public List<Stats> getRecords(@RequestParam final int duration) {
        return statsService.findAllStatsModels(duration);
    }

    @PutMapping("/{id}")
    public void incrementStats(@PathVariable final UUID id, @RequestParam final Stat stat,
        @RequestParam(defaultValue = "1") int amount) {
        statsService.incrementStat(id, stat, amount);
    }
}
