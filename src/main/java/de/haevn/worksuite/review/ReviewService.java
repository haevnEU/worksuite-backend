package de.haevn.worksuite.review;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service orchestrating CRUD operations, archiving, and query logic for {@link Review} records.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private ReviewService reviewService;
 *
 * reviewService.createReview(new CreateReviewRequestDto(
 *     "PROJ-10", "New Feature", "Summary", ReviewType.DEMO, "Demo steps", null
 * ));
 * List<ReviewResponseDto> reviews = reviewService.getReviews(false);
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * Retrieves all review items filtered by archive status and sorted by creation date.
     *
     * @param archived {@code true} for archived reviews, {@code false} for active reviews
     * @return list of {@link ReviewResponseDto} representations
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviews(final boolean archived) {
        log.info("Fetching review items with archived='{}'", archived);
        return reviewRepository.findAllByIsArchivedOrderByCreatedAtDesc(archived).stream().map(Review::toRecord)
            .map(ReviewResponseDto::fromRecord).toList();
    }

    /**
     * Creates and persists a new {@link Review} entity from the provided request payload.
     *
     * @param request creation request data
     */
    @Transactional
    public void createReview(final CreateReviewRequestDto request) {
        Objects.requireNonNull(request, "CreateReviewRequestDto must not be null");
        log.info("Creating new review entry for ticket '{}'", request.ticketNumber());

        final Review entity = Review.builder().ticketNumber(request.ticketNumber()).title(request.title())
            .description(request.description()).type(request.type()).content(request.toContentString()).build();

        reviewRepository.save(entity);
    }

    /**
     * Updates an existing {@link Review} entity by ID.
     *
     * @param id primary unique identifier of the review
     * @param request updated request data
     * @throws NotFoundException if the review entity does not exist
     */
    @Transactional
    public void updateReview(final UUID id, final CreateReviewRequestDto request) {
        Objects.requireNonNull(id, "Review ID must not be null");
        Objects.requireNonNull(request, "CreateReviewRequestDto must not be null");
        log.info("Updating review entry with ID '{}'", id);

        final Review entity = findReviewEntity(id);
        applyChanges(entity, request);
        reviewRepository.save(entity);
    }

    /**
     * Toggles the archive flag of a review item.
     *
     * @param id primary unique identifier of the review
     * @throws NotFoundException if the review entity is not found
     */
    @Transactional
    public void toggleArchive(final UUID id) {
        Objects.requireNonNull(id, "Review ID must not be null");
        log.info("Toggling archive status for review ID '{}'", id);

        final Review entity = findReviewEntity(id);
        entity.setArchived(!entity.isArchived());
        reviewRepository.save(entity);
    }

    /**
     * Deletes a review item by its unique identifier.
     *
     * @param id primary unique identifier of the review
     * @throws NotFoundException if the review does not exist
     */
    @Transactional
    public void deleteReview(final UUID id) {
        Objects.requireNonNull(id, "Review ID must not be null");
        log.info("Deleting review item with ID '{}'", id);

        if (!reviewRepository.existsById(id)) {
            throw new NotFoundException();
        }
        reviewRepository.deleteById(id);
    }

    /**
     * Resolves a {@link Review} entity by ID or throws {@link NotFoundException}.
     *
     * <p>Example usage:
     * <pre>{@code
     * Review entity = findReviewEntity(id);
     * }</pre>
     *
     * @param id target review identifier
     * @return the persistent {@link Review} entity
     * @throws NotFoundException if no review matches the ID
     */
    private Review findReviewEntity(final UUID id) {
        return reviewRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Applies payload updates from {@link CreateReviewRequestDto} onto a persistent {@link Review} entity.
     *
     * <p>Example usage:
     * <pre>{@code
     * applyChanges(reviewEntity, requestDto);
     * }</pre>
     *
     * @param entity target entity to modify
     * @param request source modification payload
     */
    private void applyChanges(final Review entity, final CreateReviewRequestDto request) {
        entity.setTicketNumber(request.ticketNumber());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setContent(request.toContentString());
    }
}