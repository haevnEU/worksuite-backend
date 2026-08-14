package de.haevn.worksuite.validation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ValidationRuleItemDto(
    @JsonIgnore String id,
    @JacksonXmlProperty(localName = "fieldName") String fieldName,
    @JacksonXmlProperty(localName = "description") String description,
    @JacksonXmlProperty(localName = "regex") String regex,
    @JacksonXmlProperty(localName = "choice") String choice,
    @JacksonXmlProperty(localName = "column") int column,
    @JacksonXmlProperty(localName = "optional") boolean optional
) {}