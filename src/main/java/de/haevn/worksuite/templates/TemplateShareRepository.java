package de.haevn.worksuite.templates;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TemplateShareRepository extends MongoRepository<Template, UUID> {
    List<Template> findByTagsContaining(final String tag);
}