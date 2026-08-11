package de.haevn.worksuite.validation;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller providing REST endpoints for compiling validation schemas into XML.
 *
 * <p>Example HTTP request:
 * <pre>{@code
 * POST /api/v1/validation
 * Content-Type: application/json
 *
 * {
 *   "readableName": "User Import",
 *   "schemaName": "user_schema",
 *   "headerIdentifier": "HDR_USER",
 *   "idColumn": 1,
 *   "idName": "userId",
 *   "totalColumns": 5,
 *   "rules": []
 * }
 * }</pre>
 */
@Log4j2
@Tag(name = "Validation Schemas", description = "Endpoints for generating XML-based file validation schemas")
@RestApiController("/api/v1/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    /**
     * Generates a formatted XML string from the provided validation schema DTO.
     *
     * @param schemaDto validation schema specification payload
     * @return {@link ResponseEntity} containing the compiled XML string
     */
    @Operation(summary = "Generate validation XML",
        description = "Serializes a validation schema object model into indented XML structure with an XML declaration.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "XML generated successfully",
        content = @Content(mediaType = MediaType.APPLICATION_XML_VALUE, schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "400", description = "Invalid schema definition payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "XML compilation error occurred",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> generateXml(@RequestBody final ValidationSchemaDto schemaDto) {
        log.info("Request received to generate XML for schema '{}'", schemaDto.schemaName());
        final String generatedXml = validationService.generateXml(schemaDto);
        return ResponseEntity.ok(generatedXml);
    }
}