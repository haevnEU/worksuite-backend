package de.haevn.worksuite.settings;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller exposing REST endpoints for inspecting and managing user accounts, third-party credentials, and avatars.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/settings/users
 * GET /api/v1/settings/users/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * PUT /api/v1/settings/users/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a/redmine-key
 * PUT /api/v1/settings/users/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a/avatar
 * GET /api/v1/settings/users/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a/avatar
 * }</pre>
 */
@Log4j2
@Tag(name = "User Settings",
    description = "Endpoints for managing user accounts, integration keys, and profile avatars")
@RestApiController("/api/v1/settings/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all user account profiles.
     *
     * @return list of {@link UserDTO} records
     */
    @Operation(summary = "Get all users", description = "Retrieves an array of all registered user profiles.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Users retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserDTO> getAllUsers() {
        log.info("Request received to fetch all users");
        return userService.getAllUsers();
    }

    /**
     * Retrieves a single user profile by its unique ID.
     *
     * @param id the user unique identifier
     * @return the found {@link UserDTO}
     */
    @Operation(summary = "Get user by ID", description = "Retrieves profile settings for a specific user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User profile found",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO getUser(
        @Parameter(description = "User unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @PathVariable final UUID id) {
        log.info("Request received to fetch user with ID: '{}'", id);
        return userService.getUserDTO(id);
    }

    /**
     * Updates the Redmine API token for a specific user.
     *
     * @param id the user unique identifier
     * @param redmineKey the API key provided in the {@code X-Redmine-API-Key} header
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Update Redmine API key",
        description = "Stores a new Redmine API key for third-party issue tracker integrations.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Redmine key updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping("/{id}/redmine-key")
    public ResponseEntity<Void> setRedmineKey(
        @Parameter(description = "User unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @PathVariable final UUID id,
        @Parameter(description = "Redmine API access token", example = "9f8e7d6c5b4a3210fedcba9876543210")
        @RequestHeader(name = "X-Redmine-API-Key") final String redmineKey) {
        log.info("Request received to set Redmine API key for user ID: '{}'", id);
        userService.setRedmineKey(id, redmineKey);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the Version Control System API token for a specific user.
     *
     * @param id the user unique identifier
     * @param vcsKey the API token provided in the {@code X-VCS-API-Key} header
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Update VCS API key", description = "Stores a new VCS token for GitLab/GitHub integrations.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "VCS key updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping("/{id}/vcs-key")
    public ResponseEntity<Void> setVcsKey(
        @Parameter(description = "User unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @PathVariable final UUID id,
        @Parameter(description = "Version Control System access token", example = "glpat-xxxxxxxxxxxxxxxxxxxx")
        @RequestHeader(name = "X-VCS-API-Key") final String vcsKey) {
        log.info("Request received to set VCS API key for user ID: '{}'", id);
        userService.setVcsKey(id, vcsKey);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads and sets the profile avatar image for a user.
     *
     * @param id the user unique identifier
     * @param file the multipart image payload
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Upload avatar", description = "Uploads and assigns a new profile avatar image for the user.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Avatar uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> setAvatar(
        @Parameter(description = "User unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @PathVariable final UUID id,
        @Parameter(description = "Avatar image file upload") @RequestParam("file") final MultipartFile file) {
        log.info("Request received to upload avatar for user ID: '{}'", id);
        userService.setAvatar(id, file);
        return ResponseEntity.noContent().build();
    }

    /**
     * Streams the profile avatar image resource for a user.
     *
     * @param id the user unique identifier
     * @return streaming binary image response
     * @throws IOException if reading the file resource fails
     */
    @Operation(summary = "Get avatar image", description = "Retrieves and streams the avatar image for a user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Avatar stream returned successfully",
        content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
        @ApiResponse(responseCode = "404", description = "Avatar image or user not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}/avatar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getAvatar(
        @Parameter(description = "User unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @PathVariable final UUID id) throws IOException {
        log.info("Request received to fetch avatar for user ID: '{}'", id);
        final Resource resource = userService.getAvatar(id);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }
}