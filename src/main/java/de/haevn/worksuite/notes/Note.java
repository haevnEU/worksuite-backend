package de.haevn.worksuite.notes;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
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