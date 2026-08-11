package de.haevn.worksuite.retro;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository providing MongoDB CRUD operations for {@link Retro} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private RetroRepository retroRepository;
 *
 * Optional<Retro> retro = retroRepository.findById(UUID.randomUUID());
 * }</pre>
 */
@Repository
public interface RetroRepository extends MongoRepository<Retro, UUID> {
}