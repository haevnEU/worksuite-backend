package de.haevn.worksuite.retro;

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
 * MongoDB document entity representing an agile sprint retrospective.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * Retro retro = Retro.builder()
 *     .id(UUID.randomUUID())
 *     .sprintName("Sprint 42")
 *     .positive(new ArrayList<>(List.of("Good team communication")))
 *     .negative(new ArrayList<>(List.of("Flaky CI pipeline")))
 *     .actionItems(new ArrayList<>(List.of("Fix integration test timeouts")))
 *     .createdAt(LocalDateTime.now())
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "retro")
public class Retro {

    @Id
    private UUID id;

    private String sprintName;

    @Builder.Default
    private List<String> positive = new ArrayList<>();

    @Builder.Default
    private List<String> negative = new ArrayList<>();

    @Builder.Default
    private List<String> actionItems = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;
}