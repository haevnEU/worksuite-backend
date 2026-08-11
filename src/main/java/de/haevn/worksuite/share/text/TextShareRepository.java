package de.haevn.worksuite.share.text;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextShareRepository extends JpaRepository<TextModel, UUID> {
    List<TextModel> findByTagsContaining(final String tag);

    List<TextModel> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(final String titleKeyword,
        final String contentKeyword);

    List<TextModel> findAllByOrderByCreatedAtDesc();

}