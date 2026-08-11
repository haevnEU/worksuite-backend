package de.haevn.worksuite.settings;

import de.haevn.worksuite.common.RestApiController;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/settings/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDTO> getAllUsers() {
        log.info("Getting all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable final UUID id) {
        log.info("Getting user with id: {}", id);
        return userService.getUser(id);
    }

    @PostMapping
    public UUID createUser(@RequestBody final UserDTO userDTO) {
        log.info("Creating user");
        return userService.createUser(userDTO);
    }

    @PutMapping("/{id}/redmine-key")
    public ResponseEntity<Void> setRedmineKey(@PathVariable final UUID id,
        @RequestHeader(name = "X-Redmine-API-Key") final String redmineKey) {
        log.info("Setting Redmine key for user with id: {}", id);
        userService.setRedmineKey(id, redmineKey);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/gitlab-key")
    public ResponseEntity<Void> setGitlabKey(@PathVariable final UUID id,
        @RequestHeader(name = "X-Gitlab-API-Key") final String gitlabKey) {
        log.info("Setting Gitlab key for user with id: {}", id);
        userService.setGitlabKey(id, gitlabKey);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/avatar")
    public ResponseEntity<Void> setAvatar(@PathVariable final UUID id, @RequestParam("file") final MultipartFile file) {
        log.info("Setting avatar for user with id: {}", id);
        userService.setAvatar(id, file);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable final UUID id) throws IOException {
        log.info("Getting avatar for user with id: {}", id);
        final Resource resource = userService.getAvatar(id);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        final MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .contentType(mediaType).body(resource);
    }
}
