package de.haevn.worksuite.share;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing persistence for {@link FileMeta} records.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private ShareRepository shareRepository;
 *
 * List<FileMeta> activeFiles = shareRepository.findAllByDeletedFalse();
 * }</pre>
 */
@Repository
public interface ShareRepository extends JpaRepository<FileMeta, UUID> {

    /**
     * Retrieves all shared files that are not marked as deleted.
     *
     * @return list of active {@link FileMeta} records
     */
    List<FileMeta> findAllByDeletedFalse();
}