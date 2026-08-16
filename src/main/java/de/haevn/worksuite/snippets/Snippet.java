package de.haevn.worksuite.snippets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "snippets")
public class Snippet {

    @Id
    private UUID id;

    private String title;

    private String content;

    private List<String> tags;

    @CreatedDate
    private LocalDateTime createdAt;

    private String language;
}