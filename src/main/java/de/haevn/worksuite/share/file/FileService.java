package de.haevn.worksuite.share.file;

import de.haevn.worksuite.common.FileStorageService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import de.haevn.worksuite.share.PasswordValidation;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Log4j2
@Service
public class FileService {
    private final FileStorageService fileStorageService;
    private final FileShareRepository fileShareRepository;
    private final WebsocketPushService websocketPushService;
    private final PasswordValidation passwordValidation;

    @Transactional
    public UUID createFileShare(final MultipartFile file, final FileShareDTO metadata) throws IOException {
        final String originalFilename = file.getOriginalFilename();
        final String contentType = file.getContentType();
        final long sizeBytes = file.getSize();

        final FileModel fileModel = metadata.toModel();
        fileModel.setOriginalFilename(originalFilename);
        fileModel.setContentType(contentType);
        fileModel.setFileSizeBytes(sizeBytes);
        final FileModel savedModel = fileShareRepository.save(fileModel);
        final UUID id = savedModel.getId();
        final String storagePath = fileStorageService.storeFile(id, file);
        savedModel.setStoragePath(storagePath);

        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "New File shared: " + savedModel.getOriginalFilename()));
        return id;
    }

    @Transactional
    public List<FileShareDTO> getAllFileShares() {
        return fileShareRepository.findAll().stream().map(FileShareDTO::fromModel).toList();
    }

    @Transactional
    public Optional<FileModel> getMetadata(final UUID id) {
        return fileShareRepository.findById(id);
    }

    @Transactional
    public Resource getFile(final UUID id, final Optional<String> password) throws IOException {
        final FileModel model = fileShareRepository.findById(id).orElseThrow(NotFoundException::new);
        passwordValidation.validatePassword(model, password);

        return fileStorageService.loadFile(model.getStoragePath());
    }


    @Transactional
    public void deleteFile(final UUID id, final Optional<String> password) {
        final FileModel model = fileShareRepository.findById(id).orElseThrow(NotFoundException::new);
        passwordValidation.validatePassword(model, password);

        fileShareRepository.deleteById(id);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "File deleted."));
    }
}
