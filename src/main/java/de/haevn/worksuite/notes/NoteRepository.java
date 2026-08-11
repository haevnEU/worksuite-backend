package de.haevn.worksuite.notes;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository providing MongoDB CRUD and text search operations for {@link Note} documents.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private NoteRepository noteRepository;
 *
 * List<Note> matches = noteRepository.findByTitleContainingIgnoreCase("Architecture");
 * }</pre>
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, UUID> {

    /**
     * Finds notes containing the specified title query string, ignoring case sensitivity.
     *
     * @param title the substring to match against note titles
     * @return a list of matching {@link Note} documents
     */
    List<Note> findByTitleContainingIgnoreCase(String title);
}