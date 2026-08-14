package de.haevn.worksuite.mock;

import de.haevn.redmine.api.InfoType;
import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.ticket.RedmineService;
import io.jsonwebtoken.lang.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;


@RequiredArgsConstructor
@Slf4j
@Service
public class MockService {

    public List<String> getSupportedMocks() {
        log.info("Getting supported mocks");
        return Collections.of(MockType.values())
                .stream()
                .map(Enum::name)
                .toList();
    }

    public Map<String, Object> getMockData(final MockType type) {
        log.info("Getting mock data for type: {}", type);
        return Map.of();
    }



}
