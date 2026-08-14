package de.haevn.worksuite.validation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

@JacksonXmlRootElement(localName = "validation")
public record ValidationSchemaDto(@JacksonXmlProperty(isAttribute = true) String readableName,
                                  @JacksonXmlProperty(isAttribute = true) String schemaName,
                                  @JacksonXmlProperty(isAttribute = true) String headerIdentifier,
                                  @JacksonXmlProperty(isAttribute = true) int idColumn,
                                  @JacksonXmlProperty(isAttribute = true) String idName,
                                  @JacksonXmlProperty(isAttribute = true) int totalColumns,
                                  @JacksonXmlElementWrapper(localName = "rules") @JacksonXmlProperty(
                                      localName = "rule") List<ValidationRuleItemDto> rules) {
}