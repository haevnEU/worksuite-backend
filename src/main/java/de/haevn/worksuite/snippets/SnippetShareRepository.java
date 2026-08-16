package de.haevn.worksuite.snippets;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SnippetShareRepository extends MongoRepository<Snippet, UUID> {
    List<Snippet> findByTagsContaining(String tag);

    List<Snippet> findByTitleContainingIgnoreCase(String title);
}