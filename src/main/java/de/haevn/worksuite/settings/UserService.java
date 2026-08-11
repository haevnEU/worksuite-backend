package de.haevn.worksuite.settings;

import de.haevn.worksuite.common.FileStorageService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

    private final WebsocketPushService websocketPushService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public List<UserDTO> getAllUsers() {
        final List<UserModel> userModels = userRepository.findAll();
        return userModels.stream().map(UserDTO::fromModel).toList();
    }

    @Transactional
    public UserDTO getUser(final UUID id) {
        final UserModel model = userRepository.findById(id).orElseThrow(NotFoundException::new);
        return UserDTO.fromModel(model);
    }

    @Transactional
    public void setRedmineKey(final UUID id, final String redmineKey) {
        final UserModel model = userRepository.findById(id).orElseThrow(NotFoundException::new);
        model.setRedmineKey(redmineKey);
        websocketPushService.dispatch(
            new WsEvent(this.getClass(), Priority.INFO, "Redmine API-Key added for user: " + id));
    }

    @Transactional
    public void setVcsKey(final UUID id, final String vcsKey) {
        final UserModel model = userRepository.findById(id).orElseThrow(NotFoundException::new);
        model.setVcsKey(vcsKey);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "VCS API-Key added for user: " + id));
    }

    @Transactional
    public void deleteUser(final UUID id) {
        final UserModel model = userRepository.findById(id).orElseThrow(NotFoundException::new);
        userRepository.deleteById(model.getId());
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "User deleted."));
    }

    @Transactional
    public void setAvatar(final UUID id, final MultipartFile file) {
        final UserModel model = userRepository.findById(id).orElseThrow(NotFoundException::new);
        try {
            final String path = fileStorageService.storeFile(id, file);
            model.setAvatarUrl(path);
            websocketPushService.dispatch(
                new WsEvent(this.getClass(), Priority.INFO, "Avatar updated for user: " + id));
        } catch (Exception e) {
            log.error("Error while setting avatar for user with id: {}", id, e);
            websocketPushService.dispatch(
                new WsEvent(this.getClass(), Priority.ERROR, "Error while setting avatar for user with id: " + id));
            throw new RuntimeException("Error while setting avatar for user with id: " + id, e);
        }
    }

    public Resource getAvatar(final UUID id) throws IOException {
        userRepository.findById(id).orElseThrow(NotFoundException::new);
        return fileStorageService.loadFile(id.toString());
    }
}
