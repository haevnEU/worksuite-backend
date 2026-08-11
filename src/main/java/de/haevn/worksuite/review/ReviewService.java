package de.haevn.worksuite.review;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviews(final boolean archived) {
        log.info("Getting reviews for archived={}", archived);
        return reviewRepository.findAllByIsArchivedOrderByCreatedAtDesc(archived).stream().map(ReviewEntity::toRecord)
            .map(ReviewResponseDto::fromRecord).toList();
    }

    @Transactional
    public void createReview(final CreateReviewRequestDto request) {
        log.info("Creating review {}", request);
        final ReviewEntity entity = new ReviewEntity();
        entity.setTicketNumber(request.ticketNumber());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setContent(request.toContentString());
        reviewRepository.save(entity);
    }

    @Transactional
    public void updateReview(final UUID id, final CreateReviewRequestDto request) {
        log.info("Updating review {}", request);
        final ReviewEntity entity = reviewRepository.findById(id).orElseThrow(NotFoundException::new);

        entity.setTicketNumber(request.ticketNumber());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setContent(request.toContentString());
        reviewRepository.save(entity);
    }

    @Transactional
    public void toggleArchive(final UUID id) {
        log.info("Toggling archived {}", id);
        final ReviewEntity entity = reviewRepository.findById(id).orElseThrow(NotFoundException::new);

        entity.setArchived(!entity.isArchived());
        reviewRepository.save(entity);
    }

    @Transactional
    public void deleteReview(final UUID id) {
        log.info("Deleting review {}", id);
        reviewRepository.deleteById(id);
    }
}