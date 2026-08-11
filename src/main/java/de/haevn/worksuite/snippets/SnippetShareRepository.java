package de.haevn.worksuite.snippets;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository managing MongoDB persistence and search queries for {@link Snippet} documents.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private SnippetShareRepository repository;
 *
 * List<Snippet> javaSnippets = repository.findByTagsContaining("java");
 * }</pre>
 */
@Repository
public interface SnippetShareRepository extends MongoRepository<Snippet, UUID> {

    /**
     * Finds snippets containing the specified tag.
     *
     * @param tag the tag to match
     * @return list of matching {@link Snippet} documents
     */
    List<Snippet> findByTagsContaining(String tag);

    /**
     * Finds snippets matching the title substring, ignoring case.
     *
     * @param title substring to search within titles
     * @return list of matching {@link Snippet} documents
     */
    List<Snippet> findByTitleContainingIgnoreCase(String title);
}