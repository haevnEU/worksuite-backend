package de.haevn.worksuite.review;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findAllByIsArchivedOrderByCreatedAtDesc(boolean isArchived);
}
