package de.haevn.worksuite.cheatsheet;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling business logic, persistence, and mapping for developer cheat sheets.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class CheatSheetService {

    private final CheatSheetRepository cheatSheetRepository;

    /**
     * Retrieves all cheat sheet items, optionally filtered by category.
     *
     * @param category category filter key or null/empty for all
     * @return list of cheat sheet response representations
     */
    @Transactional(readOnly = true)
    public List<CheatSheetResponseDto> getAllCheatSheets(final String category) {
        List<CheatSheetModel> models;

        if (category != null && !category.isBlank()) {
            log.debug("Fetching cheat sheets for category: '{}'", category);
            models = cheatSheetRepository.findAllByCategoryIgnoreCase(category.trim());
        } else {
            log.debug("Fetching all cheat sheets");
            models = cheatSheetRepository.findAll();
        }

        return models.stream()
            .map(CheatSheetMapper::toResponseDto)
            .toList();
    }

    /**
     * Retrieves a single cheat sheet by its unique identifier.
     *
     * @param id the unique cheat sheet identifier
     * @return found cheat sheet response representation
     * @throws NotFoundException if no record matches the given ID
     */
    @Transactional(readOnly = true)
    public CheatSheetResponseDto getCheatSheetById(final UUID id) {
        log.debug("Fetching cheat sheet with ID: '{}'", id);
        return cheatSheetRepository.findById(id)
            .map(CheatSheetMapper::toResponseDto)
            .orElseThrow(() -> new NotFoundException("CheatSheet not found with ID: " + id));
    }

    /**
     * Creates and persists a new cheat sheet record.
     *
     * @param requestDto validation-checked creation payload
     * @return persisted cheat sheet representation
     */
    @Transactional
    public CheatSheetResponseDto createCheatSheet(final CheatSheetRequestDto requestDto) {
        final UUID newId = UUID.randomUUID();
        final CheatSheetModel model = CheatSheetMapper.toModel(newId, requestDto);

        final CheatSheetModel saved = cheatSheetRepository.save(model);
        log.info("Created new cheat sheet '{}' with ID: '{}'", saved.getTitle(), saved.getId());

        return CheatSheetMapper.toResponseDto(saved);
    }

    /**
     * Updates an existing cheat sheet record.
     *
     * @param id         identifier of the target cheat sheet
     * @param requestDto updated attributes
     * @return updated cheat sheet representation
     * @throws NotFoundException if no record matches the given ID
     */
    @Transactional
    public CheatSheetResponseDto updateCheatSheet(final UUID id, final CheatSheetRequestDto requestDto) {
        final CheatSheetModel existing = cheatSheetRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("CheatSheet not found with ID: " + id));

        final CheatSheetModel updatedModel = CheatSheetMapper.toModel(id, requestDto);
        updatedModel.setCreatedAt(existing.getCreatedAt());
        updatedModel.setUpdatedAt(LocalDateTime.now());

        final CheatSheetModel saved = cheatSheetRepository.save(updatedModel);
        log.info("Updated cheat sheet with ID: '{}'", saved.getId());

        return CheatSheetMapper.toResponseDto(saved);
    }

    /**
     * Permanently removes a cheat sheet entry by ID.
     *
     * @param id target cheat sheet identifier
     * @throws NotFoundException if no record exists for deletion
     */
    @Transactional
    public void deleteCheatSheet(final UUID id) {
        if (!cheatSheetRepository.existsById(id)) {
            throw new NotFoundException("CheatSheet not found with ID: " + id);
        }

        cheatSheetRepository.deleteById(id);
        log.info("Successfully deleted cheat sheet with ID: '{}'", id);
    }
}