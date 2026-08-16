package de.haevn.worksuite.retro;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "retro")
public class Retro {

    @Id
    private UUID id;

    private String sprintName;

    private List<String> positive;

    private List<String> negative;

    private List<String> actionItems;

    @CreatedDate
    private LocalDateTime createdAt;
}