package de.haevn.worksuite.info;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.ticket.TicketProviderType;
import de.haevn.worksuite.ticket.TicketService;
import de.haevn.worksuite.ticket.dtos.InfoResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service aggregating configuration and metadata catalogues from Redmine.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private InfoService infoService;
 *
 * Map<String, List<RedmineInfoResponses.InfoResponse>> info = infoService.getRedmineInfo();
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InfoService {

    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PRIORITY = "priority";

    private final TicketService ticketService;

    /**
     * Loads and aggregates activities, issue statuses, and priorities from the configured Redmine instance.
     *
     * @return map of category names to lists of {@link RedmineInfoResponses.InfoResponse} entries
     * @throws RedmineException if communication with the Redmine API fails
     */
    public Map<String, List<InfoResponse>> getRedmineInfo(final TicketProviderType provider) {
        log.info("Fetching Redmine enumeration and metadata catalogues");

        final var activities = ticketService.getInfo(provider, InfoType.ACTIVITY);
        final var statuses = ticketService.getInfo(provider, InfoType.STATUS);
        final var priorities = ticketService.getInfo(provider, InfoType.PRIORITY);

        return Map.of(KEY_ACTIVITY, activities, KEY_STATUS, statuses, KEY_PRIORITY, priorities);
    }
}