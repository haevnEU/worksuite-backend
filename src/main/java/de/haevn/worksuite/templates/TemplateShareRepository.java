package de.haevn.worksuite.templates;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository managing MongoDB persistence and search queries for {@link Template} documents.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private TemplateShareRepository repository;
 *
 * List<Template> gitTemplates = repository.findByTagsContaining("git");
 * }</pre>
 */
@Repository
public interface TemplateShareRepository extends MongoRepository<Template, UUID> {

    /**
     * Finds templates containing the specified tag.
     *
     * @param tag the tag substring to match
     * @return list of matching {@link Template} documents
     */
    List<Template> findByTagsContaining(String tag);
}