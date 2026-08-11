package de.haevn.worksuite.notes;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document entity representing a personal or shared note.
 *
 * <p>Example usage:
 * <pre>{@code
 * Note note = Note.builder()
 *     .id(UUID.randomUUID())
 *     .title("Database Migration Plan")
 *     .content("Step 1: Run flyway scripts...")
 *     .ticketId("TICK-4021")
 *     .createdAt(LocalDateTime.now())
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
public class Note {

    @Id
    private UUID id;

    private String title;

    private String content;

    private String ticketId;

    @CreatedDate
    private LocalDateTime createdAt;
}