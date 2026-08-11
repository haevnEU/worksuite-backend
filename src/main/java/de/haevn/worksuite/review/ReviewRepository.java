package de.haevn.worksuite.review;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing database persistence and querying for {@link Review} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private ReviewRepository reviewRepository;
 *
 * List<Review> active = reviewRepository.findAllByIsArchivedOrderByCreatedAtDesc(false);
 * }</pre>
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Finds review entries matching the specified archive status, ordered by creation date descending.
     *
     * @param isArchived {@code true} to retrieve archived items, {@code false} for active items
     * @return list of matching {@link Review} entities
     */
    List<Review> findAllByIsArchivedOrderByCreatedAtDesc(boolean isArchived);
}