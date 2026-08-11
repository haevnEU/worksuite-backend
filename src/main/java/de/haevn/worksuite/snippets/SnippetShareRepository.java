package de.haevn.worksuite.snippets;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SnippetShareRepository extends MongoRepository<SnippetModel, UUID> {
    List<SnippetModel> findByTagsContaining(String tag);
    List<SnippetModel> findByTitleContainingIgnoreCase(String title);
}