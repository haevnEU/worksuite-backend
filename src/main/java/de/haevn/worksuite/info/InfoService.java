package de.haevn.worksuite.info;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.ticket.RedmineService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Slf4j
@Service
class InfoService {
    private final RedmineService redmineService;

    public Map<String, List<RedmineInfoResponses.InfoResponse>> getRedmineInfo() throws RedmineException {
        log.info("Getting redmine info");
        final Map<String, List<RedmineInfoResponses.InfoResponse>> infoResponseMap = new HashMap<>();
        final var activity = redmineService.getInfo(InfoType.ACTIVITY);
        final var stratus = redmineService.getInfo(InfoType.STATUS);
        final var priority = redmineService.getInfo(InfoType.PRIORITY);

        infoResponseMap.put("activity", activity);
        infoResponseMap.put("status", stratus);
        infoResponseMap.put("priority", priority);

        return infoResponseMap;
    }
}
