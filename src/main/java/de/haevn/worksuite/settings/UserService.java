package de.haevn.worksuite.settings;

import de.haevn.worksuite.common.FileStorageService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service managing user profiles, integration keys, avatar storage, and account lifecycles.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private UserService userService;
 *
 * UserDTO user = userService.getUserDTO(userId);
 * userService.setRedmineKey(userId, "api-key-12345");
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String EVENT_REDMINE_KEY_ADDED = "Redmine API-Key added for user: %s";
    private static final String EVENT_VCS_KEY_ADDED = "VCS API-Key added for user: %s";
    private static final String EVENT_AVATAR_UPDATED = "Avatar updated for user: %s";
    private static final String EVENT_AVATAR_ERROR = "Error while setting avatar for user with id: %s";
    private static final String EVENT_USER_DELETED = "User deleted.";

    private final WebsocketPushService websocketPushService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    /**
     * Retrieves all registered users as sanitized DTOs.
     *
     * @return list of {@link UserDTO} records
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::fromModel).toList();
    }

    /**
     * Retrieves a user profile as a sanitized DTO by ID.
     *
     * @param id the unique user identifier
     * @return the {@link UserDTO}
     * @throws NotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserDTO(final UUID id) {
        return UserDTO.fromModel(getUser(id));
    }

    /**
     * Resolves the persistent {@link UserModel} entity by ID.
     *
     * @param id the unique user identifier
     * @return the persistent {@link UserModel} entity
     * @throws NotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserModel getUser(final UUID id) {
        Objects.requireNonNull(id, "User ID must not be null");
        return userRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Configures the Redmine API token for a user.
     *
     * @param id the unique user identifier
     * @param redmineKey the API access token
     */
    @Transactional
    public void setRedmineKey(final UUID id, final String redmineKey) {
        final UserModel model = getUser(id);
        model.setRedmineKey(redmineKey);
        userRepository.save(model);

        broadcastEvent(Priority.INFO, EVENT_REDMINE_KEY_ADDED.formatted(id));
    }

    /**
     * Configures the Version Control System API token for a user.
     *
     * @param id the unique user identifier
     * @param vcsKey the API access token
     */
    @Transactional
    public void setVcsKey(final UUID id, final String vcsKey) {
        final UserModel model = getUser(id);
        model.setVcsKey(vcsKey);
        userRepository.save(model);

        broadcastEvent(Priority.INFO, EVENT_VCS_KEY_ADDED.formatted(id));
    }

    /**
     * Stores and updates a user's avatar image.
     *
     * @param id the unique user identifier
     * @param file the avatar image payload
     */
    @Transactional
    public void setAvatar(final UUID id, final MultipartFile file) {
        Objects.requireNonNull(file, "Avatar MultipartFile must not be null");
        final UserModel model = getUser(id);

        try {
            final String storedPath = fileStorageService.storeFile(id, file);
            model.setAvatarUrl(storedPath);
            userRepository.save(model);

            broadcastEvent(Priority.INFO, EVENT_AVATAR_UPDATED.formatted(id));
        } catch (Exception ex) {
            log.error("Failed to store avatar for user ID: '{}'", id, ex);
            broadcastEvent(Priority.ERROR, EVENT_AVATAR_ERROR.formatted(id));
            throw new IllegalStateException("Error while setting avatar for user with id: " + id, ex);
        }
    }

    /**
     * Loads a user's avatar image from local storage.
     *
     * @param id the unique user identifier
     * @return readable {@link Resource} of the avatar file
     * @throws IOException if reading the file fails
     */
    @Transactional(readOnly = true)
    public Resource getAvatar(final UUID id) throws IOException {
        getUser(id);
        return fileStorageService.loadFile(id.toString());
    }

    /**
     * Deletes a user profile and broadcasts a notification.
     *
     * @param id the unique user identifier
     */
    @Transactional
    public void deleteUser(final UUID id) {
        final UserModel model = getUser(id);
        userRepository.deleteById(model.getId());
        broadcastEvent(Priority.INFO, EVENT_USER_DELETED);
    }

    /**
     * Verifies if a user's account license has expired.
     *
     * @param id the unique user identifier
     * @return {@code true} if the license expired, {@code false} if valid
     */
    @Transactional(readOnly = true)
    public boolean licenseExpired(final UUID id) {
        final UserModel model = getUser(id);
        return model.getLicenseExpiration() == null || model.getLicenseExpiration().isBefore(Instant.now());
    }

    /**
     * Helper method to dispatch WebSocket events.
     *
     * <p>Example usage:
     * <pre>{@code
     * broadcastEvent(Priority.INFO, "User updated");
     * }</pre>
     *
     * @param priority severity level
     * @param message notification message
     */
    private void broadcastEvent(final Priority priority, final String message) {
        websocketPushService.dispatch(new WsEvent(getClass(), priority, message));
    }
}