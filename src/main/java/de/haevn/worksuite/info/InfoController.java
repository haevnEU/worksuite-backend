package de.haevn.worksuite.info;

import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@RestApiController("/api/v1/info")
public class InfoController {
    private final InfoService infoService;

    @GetMapping("/redmine")
    public Map<String, List<RedmineInfoResponses.InfoResponse>> getRedmineInfo() throws RedmineException {
        return infoService.getRedmineInfo();
    }

}
