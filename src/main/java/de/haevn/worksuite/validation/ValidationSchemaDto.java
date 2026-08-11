package de.haevn.worksuite.validation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Data transfer object representing the complete XML validation configuration schema.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * ValidationSchemaDto schema = new ValidationSchemaDto(
 *     "Order Import Specification",
 *     "order_import_v1",
 *     "HEADER_ORDER_V1",
 *     1,
 *     "orderId",
 *     8,
 *     List.of(new ValidationRuleItemDto(null, "orderId", "Unique ID", "^\\d+$", null, 1, false))
 * );
 * }</pre>
 *
 * @param readableName human-readable descriptive schema label
 * @param schemaName system identifier of the schema
 * @param headerIdentifier header line identifier marker
 * @param idColumn 1-based column position containing the unique record identifier
 * @param idName name attribute of the identity column
 * @param totalColumns total required column count
 * @param rules list of {@link ValidationRuleItemDto} rule definitions
 */
@JacksonXmlRootElement(localName = "validation")
@Schema(description = "Root schema definition for XML-based file validation")
public record ValidationSchemaDto(

    @JacksonXmlProperty(isAttribute = true) @Schema(description = "Display label of the schema",
        example = "Customer Master Data Import") String readableName,

    @JacksonXmlProperty(isAttribute = true) @Schema(description = "Technical schema identifier",
        example = "customer_import_schema") String schemaName,

    @JacksonXmlProperty(isAttribute = true) @Schema(description = "Header line prefix identifier",
        example = "HDR_CUST_V2") String headerIdentifier,

    @JacksonXmlProperty(isAttribute = true) @Schema(description = "Column index containing primary identifier",
        example = "1") int idColumn,

    @JacksonXmlProperty(isAttribute = true) @Schema(description = "Field name of the ID column",
        example = "customerId") String idName,

    @JacksonXmlProperty(isAttribute = true) @Schema(description = "Expected total number of columns",
        example = "12") int totalColumns,

    @JacksonXmlElementWrapper(localName = "rules") @JacksonXmlProperty(localName = "rule") @ArraySchema(
        schema = @Schema(implementation = ValidationRuleItemDto.class)) List<ValidationRuleItemDto> rules) {
}