package de.haevn.worksuite.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import de.haevn.worksuite.common.exceptions.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ValidationService {
    private final XmlMapper xmlMapper;

    public ValidationService() {
        this.xmlMapper = XmlMapper.builder().enable(SerializationFeature.INDENT_OUTPUT)
            .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true).build();
    }

    public String generateXml(ValidationSchemaDto dto) {
        try {
            return xmlMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Failed to generate XML from ValidationSchemaDto", e);
        }
    }
}
