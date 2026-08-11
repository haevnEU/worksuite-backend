package de.haevn.worksuite.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    List<ReviewEntity> findAllByIsArchivedOrderByCreatedAtDesc(boolean isArchived);
}
