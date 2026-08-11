package de.haevn.worksuite.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import de.haevn.worksuite.common.exceptions.InternalServerErrorException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service generating formatted XML definitions from {@link ValidationSchemaDto} models.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private ValidationService validationService;
 *
 * String xml = validationService.generateXml(schemaDto);
 * }</pre>
 */
@Slf4j
@Service
public class ValidationService {

    private final XmlMapper xmlMapper;

    /**
     * Constructs the {@link ValidationService} and initializes the customized {@link XmlMapper}.
     */
    public ValidationService() {
        this.xmlMapper = createConfiguredXmlMapper();
    }

    /**
     * Builds and configures an {@link XmlMapper} instance with indentation and XML declaration features enabled.
     *
     * <p>Example usage:
     * <pre>{@code
     * XmlMapper mapper = createConfiguredXmlMapper();
     * }</pre>
     *
     * @return pre-configured {@link XmlMapper}
     */
    private static XmlMapper createConfiguredXmlMapper() {
        return XmlMapper.builder().enable(SerializationFeature.INDENT_OUTPUT)
            .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true).build();
    }

    /**
     * Converts a {@link ValidationSchemaDto} into formatted XML with an XML declaration header.
     *
     * @param dto the schema definition model
     * @return formatted XML string
     * @throws InternalServerErrorException if XML generation fails
     */
    public String generateXml(final ValidationSchemaDto dto) {
        Objects.requireNonNull(dto, "ValidationSchemaDto must not be null");
        try {
            return xmlMapper.writeValueAsString(dto);
        } catch (final JsonProcessingException ex) {
            log.error("Error serializing ValidationSchemaDto to XML for schema: '{}'", dto.schemaName(), ex);
            throw new InternalServerErrorException("Failed to generate XML from ValidationSchemaDto", ex);
        }
    }
}