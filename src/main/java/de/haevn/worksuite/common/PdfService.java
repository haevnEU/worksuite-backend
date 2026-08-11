package de.haevn.worksuite.common;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for rendering Thymeleaf HTML templates into PDF documents.
 *
 * <p>Example usage:
 * <pre>{@code
 * Map<String, Object> context = Map.of("customerName", "Max Mustermann", "total", 199.99);
 * Resource pdfResource = pdfService.generatePdfResource("reports/invoice", context, false);
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PdfService {

    private static final String STATIC_RESOURCE_PATH = "/static/";
    private static final String VAR_EXPORT_DATE = "headerExportDate";
    private static final String VAR_IS_DRAFT = "isDraft";

    private final TemplateEngine templateEngine;

    /**
     * Generates a downloadable {@link Resource} containing the rendered PDF.
     *
     * @param templateName the Thymeleaf template path (e.g., {@code "invoices/summary"})
     * @param variables key-value parameters passed to the template context
     * @param isDraft whether a draft indicator should be added to file prefix and context
     * @return a {@link ByteArrayResource} configured with the appropriate filename
     */
    public Resource generatePdfResource(final String templateName, final Map<String, Object> variables,
        final boolean isDraft) {
        Objects.requireNonNull(templateName, "Template name must not be null");

        final byte[] pdfBytes = generatePdf(templateName, variables, isDraft);
        final String rawFileName = Paths.get(templateName).getFileName().toString();
        final String filename = "%s%s.pdf".formatted(isDraft ? "DRAFT_" : "", rawFileName);

        return new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    /**
     * Processes a Thymeleaf template and converts the resulting HTML to a PDF byte array.
     *
     * @param templateName the template location relative to Thymeleaf's template resolver
     * @param variables variables injected into the template
     * @param isDraft draft flag passed to the rendering context
     * @return the generated PDF binary data
     * @throws IllegalStateException if rendering or template processing fails
     */
    public byte[] generatePdf(final String templateName, final Map<String, Object> variables, final boolean isDraft) {
        Objects.requireNonNull(templateName, "Template name must not be null");

        try {
            final Map<String, Object> contextVariables = variables != null ? new HashMap<>(variables) : new HashMap<>();
            contextVariables.put(VAR_EXPORT_DATE, LocalDateTime.now());
            contextVariables.putIfAbsent(VAR_IS_DRAFT, isDraft);

            final Context context = new Context();
            context.setVariables(contextVariables);

            final String htmlContent = templateEngine.process(templateName, context);
            return renderHtmlToPdf(htmlContent);
        } catch (Exception ex) {
            log.error("Failed to generate PDF for template '{}'", templateName, ex);
            throw new IllegalStateException("Error while generating PDF for template: " + templateName, ex);
        }
    }

    /**
     * Renders an HTML string into a PDF byte array using {@link com.openhtmltopdf.pdfboxout.PdfRendererBuilder}.
     *
     * <p>Resolves relative static assets (such as CSS or images) via the static classpath resource path.
     *
     * <p>Example usage:
     * <pre>{@code
     * String html = "<html><body><h1>Invoice #1024</h1></body></html>";
     * byte[] pdfData = renderHtmlToPdf(html);
     * }</pre>
     *
     * @param htmlContent the raw HTML string to convert into PDF format
     * @return the rendered PDF document as a byte array
     * @throws IOException if loading base static resources fails or an I/O stream error occurs
     */
    private byte[] renderHtmlToPdf(final String htmlContent) throws IOException {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final String baseUrl = new ClassPathResource(STATIC_RESOURCE_PATH).getURL().toExternalForm();

            final PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, baseUrl);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        }
    }
}