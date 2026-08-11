package de.haevn.worksuite.stats;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.time.TimeModel;
import de.haevn.worksuite.time.TimeService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class StatsService {

    final StatsRepository statsRepository;
    final TimeService timeService;

    @Transactional
    public StatsModel findStatsModelById(final UUID id) {
        return statsRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public List<StatsModel> findAllStatsModels(final int limit) {
        final int amount = (limit > 0) ? limit : 7;

        final List<TimeModel> timeModels = timeService.getAll(amount);
        final List<StatsModel> modelList = statsRepository.findStatsBefore(Instant.now(), amount);

        final Map<LocalDate, Integer> hoursPerDay = timeModels.stream().filter(time -> time.getDate() != null).collect(
            Collectors.groupingBy(time -> time.getDate().atZone(ZoneOffset.UTC).toLocalDate(),
                Collectors.summingInt(time -> {
                    final int totalMinutes = (time.getHours() * 60) + time.getMinutes();
                    return totalMinutes / 60;
                })));

        modelList.forEach(stats -> {
            if (stats.getDay() != null) {
                LocalDate statsDate = stats.getDay().atZone(ZoneOffset.UTC).toLocalDate();
                int calculatedHours = hoursPerDay.getOrDefault(statsDate, 0);
                stats.setHoursSpent(calculatedHours);
            }
        });

        statsRepository.saveAll(modelList);
        return modelList;
    }

    @Transactional
    public UUID createNewRecord() {
        return createNewRecord(Instant.now());
    }

    @Transactional
    public UUID createNewRecord(final Instant date) {
        StatsModel statsModel = new StatsModel();
        statsModel.setDay(date);
        statsModel.setHoursSpent(0);
        statsModel.setMovedToQa(0);
        statsModel.setMovedToReview(0);
        statsModel.setReturnFromQa(0);
        statsModel.setReturnFromReview(0);
        return statsRepository.save(statsModel).getId();
    }

    @Transactional
    public void incrementStat(final UUID uuid, final Stat stat, final int amount) {
        StatsModel statsModel = findStatsModelById(uuid);
        final Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        if (statsModel.getDay() != null && statsModel.getDay().isBefore(startOfToday)) {
            statsModel = new StatsModel();
            statsModel.setDay(Instant.now());
        }
        switch (stat) {
            case MOVED_TO_QA -> statsModel.setMovedToQa(statsModel.getMovedToQa() + amount);
            case RETURN_FROM_QA -> statsModel.setReturnFromQa(statsModel.getReturnFromQa() + amount);
            case MOVED_TO_REVIEW -> statsModel.setMovedToReview(statsModel.getMovedToReview() + amount);
            case RETURN_FROM_REVIEW -> statsModel.setReturnFromReview(statsModel.getReturnFromReview() + amount);
        }
        statsRepository.save(statsModel);
    }
}
