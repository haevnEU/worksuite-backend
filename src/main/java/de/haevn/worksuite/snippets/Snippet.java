package de.haevn.worksuite.snippets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document entity representing a reusable code or text snippet.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * Snippet snippet = Snippet.builder()
 *     .id(UUID.randomUUID())
 *     .title("PostgreSQL Docker Compose")
 *     .language("yaml")
 *     .content("services:\n  postgres:\n    image: postgres:16")
 *     .tags(List.of("docker", "database"))
 *     .createdAt(LocalDateTime.now())
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "snippets")
public class Snippet {

    @Id
    private UUID id;

    private String title;

    private String content;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private String language;

    @CreatedDate
    private LocalDateTime createdAt;
}