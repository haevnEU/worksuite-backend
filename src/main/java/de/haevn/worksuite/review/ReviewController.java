package de.haevn.worksuite.review;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewResponseDto> getReviews(@RequestParam(defaultValue = "false") final boolean archived) {
        return reviewService.getReviews(archived);
    }

    // TODO Migrate all to this
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createReview(@Valid @RequestBody final CreateReviewRequestDto request) {
        reviewService.createReview(request);
    }

    @PutMapping("/{id}")
    public void updateReview(@PathVariable final UUID id, @Valid @RequestBody final CreateReviewRequestDto request) {
        reviewService.updateReview(id, request);
    }

    @PatchMapping("/{id}/archive")
    public void toggleArchive(@PathVariable final UUID id) {
        reviewService.toggleArchive(id);
    }

    // TODO Migrate all to this
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable final UUID id) {
        reviewService.deleteReview(id);
    }
}
