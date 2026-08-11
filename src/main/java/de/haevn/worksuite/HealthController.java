package de.haevn.worksuite;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    public HealthController() {
    }

    @GetMapping("/api/health")
    public Map<String, Object> healthCheck(@RequestHeader HttpHeaders headers) {
        Map<String, String> headerMap = headers.toSingleValueMap();
        return Map.of("status", "UP", "message", "Backend is running fine!", "headers", headerMap);
    }
}