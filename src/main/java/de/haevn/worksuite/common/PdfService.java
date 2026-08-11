package de.haevn.worksuite.common;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Log4j2
@RequiredArgsConstructor
@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public Resource generatePdfResource(final String templateName, final Map<String, Object> variables, final boolean isDraft) {
        final byte[] pdfBytes = generatePdf(templateName, variables, isDraft);
        final String cleanFilename = templateName.contains("/")
            ? templateName.substring(templateName.lastIndexOf("/") + 1)
            : templateName;

        return new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return (isDraft ? "DRAFT" : "") + cleanFilename + ".pdf";
            }
        };
    }

    public byte[] generatePdf(final String templateName, final Map<String, Object> variables, final boolean isDraft) {
        try {
            final Map<String, Object> contextVariables = variables != null ? new HashMap<>(variables) : new HashMap<>();
            contextVariables.replace("headerExportDate", LocalDateTime.now());

            final Context context = new Context();
            contextVariables.putIfAbsent("isDraft", isDraft);
            context.setVariables(contextVariables);
            final String htmlContent = templateEngine.process(templateName, context);
            return renderHtmlToPdf(htmlContent);
        } catch (Exception e) {
            log.error("Fehler bei der PDF-Generierung für Template: {}", templateName, e);
            throw new RuntimeException("Fehler bei der PDF-Generierung für Template: " + templateName, e);
        }
    }

    private byte[] renderHtmlToPdf(final String htmlContent) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final String baseUrl = new ClassPathResource("/static/").getURL().toExternalForm();

            final PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, baseUrl);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        }
    }
}