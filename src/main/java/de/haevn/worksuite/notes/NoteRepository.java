package de.haevn.worksuite.notes;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoteRepository extends MongoRepository<NoteModel, UUID> {
    List<NoteModel> findByTitleContainingIgnoreCase(final String title);
}