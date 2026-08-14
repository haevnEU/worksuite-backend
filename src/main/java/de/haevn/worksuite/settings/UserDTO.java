package de.haevn.worksuite.settings;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(UUID id, String firstName, String lastName, String role, String redmineKey, String vcsKey,
                      Instant createdAt, String avatarUrl) {

    public static UserDTO fromModel(final UserModel userModel) {
        final String vcsKey = (userModel.getVcsKey() == null || userModel.getVcsKey().isEmpty()) ? null : "<REDACTED>";
        final String redmineKey =
            (userModel.getRedmineKey() == null || userModel.getRedmineKey().isEmpty()) ? null : "<REDACTED>";
        return new UserDTO(userModel.getId(), userModel.getFirstName(), userModel.getLastName(), userModel.getRole(),
            redmineKey, vcsKey, userModel.getCreatedAt(), userModel.getAvatarUrl());
    }

    public UserModel toModel() {
        final UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setFirstName(firstName);
        userModel.setLastName(lastName);
        userModel.setRole(role);
        userModel.setRedmineKey(redmineKey);
        userModel.setVcsKey(vcsKey);
        userModel.setCreatedAt(createdAt);
        userModel.setAvatarUrl(avatarUrl);
        return userModel;
    }
}
