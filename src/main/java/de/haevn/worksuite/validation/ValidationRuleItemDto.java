package de.haevn.worksuite.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data transfer object defining a single column validation rule within an XML validation schema.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * ValidationRuleItemDto rule = new ValidationRuleItemDto(
 *     "rule-1",
 *     "emailAddress",
 *     "Must be a valid corporate email format",
 *     "^[A-Za-z0-9._%+-]+@hausheld\\.info$",
 *     null,
 *     3,
 *     false
 * );
 * }</pre>
 *
 * @param id internal identifier ignored during XML serialization
 * @param fieldName logical name of the validated column field
 * @param description rule description or error explanation
 * @param regex regular expression pattern applied for format validation
 * @param choice predefined set of accepted string choices
 * @param column 1-based column index position
 * @param optional indicates whether empty or missing values are permitted
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "Validation rule specification for an individual schema column")
public record ValidationRuleItemDto(

    @JsonIgnore @Schema(hidden = true) String id,

    @JacksonXmlProperty(localName = "fieldName") @Schema(description = "Name of the target field to validate",
        example = "orderNumber", requiredMode = Schema.RequiredMode.REQUIRED) String fieldName,

    @JacksonXmlProperty(localName = "description") @Schema(
        description = "Human-readable description or requirement details",
        example = "Must match standard 8-digit order number") String description,

    @JacksonXmlProperty(localName = "regex") @Schema(description = "Regex pattern enforced on column values",
        example = "^ORD-\\d{8}$") String regex,

    @JacksonXmlProperty(localName = "choice") @Schema(
        description = "Comma-separated or pipe-separated allowed discrete values",
        example = "ACTIVE|INACTIVE|PENDING") String choice,

    @JacksonXmlProperty(localName = "column") @Schema(description = "Column index location in the file", example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED) int column,

    @JacksonXmlProperty(localName = "optional") @Schema(description = "Whether the field is optional",
        example = "false", defaultValue = "false") boolean optional) {
}