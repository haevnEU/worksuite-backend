package de.haevn.worksuite.time;


import de.haevn.worksuite.ticket.LogTimeRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class TimeService {
    private final TimeRepository timeRepository;

    public List<TimeEntry> getForToday() {
        final Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant endOfToday = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return timeRepository.findByDateBetweenOrderByDateDesc(startOfToday, endOfToday);
    }

    public List<TimeEntry> getAll(final int historyDays) {
        final Instant startThreshold =
            LocalDate.now().minusDays(historyDays).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return timeRepository.findEntriesFromDate(startThreshold);
    }

    @Transactional
    public void book(final long ticketId, final LogTimeRequest timeDTO) {
        final TimeEntry timeEntry = new TimeEntry();
        timeEntry.setDescription(timeDTO.comment());
        timeEntry.setHours(timeDTO.hours());
        timeEntry.setMinutes(timeDTO.minutes());
        timeEntry.setDate(LocalDate.parse(timeDTO.day()).atStartOfDay(ZoneId.systemDefault()).toInstant());
        timeEntry.setActivityId(timeDTO.activityId());
        timeEntry.setTicketId(ticketId);
        timeRepository.save(timeEntry);
    }
}
