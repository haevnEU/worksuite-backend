package de.haevn.worksuite.info;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.ticket.RedmineService;
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

    private final RedmineService redmineService;

    /**
     * Loads and aggregates activities, issue statuses, and priorities from the configured Redmine instance.
     *
     * @return map of category names to lists of {@link RedmineInfoResponses.InfoResponse} entries
     * @throws RedmineException if communication with the Redmine API fails
     */
    public Map<String, List<RedmineInfoResponses.InfoResponse>> getRedmineInfo() throws RedmineException {
        log.info("Fetching Redmine enumeration and metadata catalogues");

        final var activities = fetchInfo(InfoType.ACTIVITY);
        final var statuses = fetchInfo(InfoType.STATUS);
        final var priorities = fetchInfo(InfoType.PRIORITY);

        return Map.of(KEY_ACTIVITY, activities, KEY_STATUS, statuses, KEY_PRIORITY, priorities);
    }

    /**
     * Fetches metadata entries for a single {@link InfoType}.
     *
     * <p>Example usage:
     * <pre>{@code
     * List<RedmineInfoResponses.InfoResponse> statuses = fetchInfo(InfoType.STATUS);
     * }</pre>
     *
     * @param type the specific metadata category to request
     * @return list of metadata entries returned by Redmine
     * @throws RedmineException if the remote API call fails
     */
    private List<RedmineInfoResponses.InfoResponse> fetchInfo(final InfoType type) throws RedmineException {
        return redmineService.getInfo(type);
    }
}