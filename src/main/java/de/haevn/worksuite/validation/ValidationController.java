package de.haevn.worksuite.validation;

import de.haevn.worksuite.common.RestApiController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestApiController("/api/v1/validation")
public class ValidationController {
    private final ValidationService validationService;

    @PostMapping
    public ResponseEntity<String> generateXml(@RequestBody final ValidationSchemaDto schemaDto) {
        String generatedXml = validationService.generateXml(schemaDto);
        return ResponseEntity.ok(generatedXml);
    }
}
