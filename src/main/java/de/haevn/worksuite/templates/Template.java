package de.haevn.worksuite.templates;

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
 * MongoDB document entity representing a reusable text or boilerplate template.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * Template template = Template.builder()
 *     .id(UUID.randomUUID())
 *     .title("Merge Request Template")
 *     .content("## Summary\n\n## Checklist\n- [ ] Tested locally")
 *     .tags(List.of("git", "review"))
 *     .platform("GitLab")
 *     .createdAt(LocalDateTime.now())
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "templates")
public class Template {

    @Id
    private UUID id;

    private String title;

    private String content;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private String platform;

    @CreatedDate
    private LocalDateTime createdAt;
}