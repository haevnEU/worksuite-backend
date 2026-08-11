package de.haevn.worksuite.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.util.StringUtils;

/**
 * Data transfer object representing user profile settings with redacted sensitive API keys.
 *
 * <p>Example usage:
 * <pre>{@code
 * UserDTO dto = UserDTO.fromModel(userEntity);
 * }</pre>
 *
 * @param id the unique user identifier
 * @param firstName first name of the user
 * @param lastName last name of the user
 * @param role authorization role assigned to the user
 * @param redmineKey indicator if a Redmine key is configured (redacted value)
 * @param vcsKey indicator if a VCS key is configured (redacted value)
 * @param createdAt instant when the user profile was created
 * @param avatarUrl local storage reference or URL to the user's avatar image
 */
@Schema(description = "User account profile and settings representation")
public record UserDTO(

    @Schema(description = "Unique user identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") UUID id,

    @Schema(description = "User's first name", example = "Nils") String firstName,

    @Schema(description = "User's last name", example = "Milewski") String lastName,

    @Schema(description = "Assigned user role", example = "ADMIN") String role,

    @Schema(description = "Masked status of the Redmine API key", example = "<REDACTED>") String redmineKey,

    @Schema(description = "Masked status of the Version Control System API key", example = "<REDACTED>") String vcsKey,

    @Schema(description = "Account registration timestamp", example = "2026-08-17T18:00:00.000Z") Instant createdAt,

    @Schema(description = "Avatar resource storage identifier",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") String avatarUrl) {

    private static final String REDACTED = "<REDACTED>";

    /**
     * Converts a {@link UserModel} entity into a sanitized {@link UserDTO}, masking sensitive API keys.
     *
     * <p>Example:
     * <pre>{@code
     * UserDTO dto = UserDTO.fromModel(userModel);
     * }</pre>
     *
     * @param userModel the source {@link UserModel}
     * @return the sanitized {@link UserDTO}
     */
    public static UserDTO fromModel(final UserModel userModel) {
        Objects.requireNonNull(userModel, "UserModel must not be null");

        final String maskedVcsKey = StringUtils.hasText(userModel.getVcsKey()) ? REDACTED : null;
        final String maskedRedmineKey = StringUtils.hasText(userModel.getRedmineKey()) ? REDACTED : null;

        return new UserDTO(userModel.getId(), userModel.getFirstName(), userModel.getLastName(), userModel.getRole(),
            maskedRedmineKey, maskedVcsKey, userModel.getCreatedAt(), userModel.getAvatarUrl());
    }

    /**
     * Converts this DTO into a {@link UserModel} entity.
     *
     * <p>Example:
     * <pre>{@code
     * UserModel model = userDTO.toModel();
     * }</pre>
     *
     * @return the populated {@link UserModel}
     */
    public UserModel toModel() {
        return UserModel.builder().id(id).firstName(firstName).lastName(lastName).role(role).redmineKey(redmineKey)
            .vcsKey(vcsKey).createdAt(createdAt).avatarUrl(avatarUrl).build();
    }
}