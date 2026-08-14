package de.haevn.worksuite.share;


import de.haevn.worksuite.common.FileStorageService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.common.exceptions.StorageException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Log4j2
public class ShareService {

    private final FileStorageService fileStorageService;
    private final ShareRepository shareRepository;

    public void share(final List<MultipartFile> files) {
        log.info("Sharing files: {}", files.stream().map(MultipartFile::getOriginalFilename).toList());
        files.forEach(this::storeFile);
    }

    public List<FileMeta> getAllSharedFiles() {
        return shareRepository.findAll();
    }

    public ResponseEntity<Resource> downloadFile(final UUID id) throws IOException {
        final Resource resource = fileStorageService.loadFile(id.toString());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Transactional
    public void storeFile(final MultipartFile file) {
        try {
            final FileMeta meta = new FileMeta();
            meta.setFilename(file.getOriginalFilename());
            meta.setFileType(file.getContentType());
            meta.setFileSize(file.getSize());
            meta.setChecksum(fileStorageService.calculateChecksum(file));
            final FileMeta savedEntity = shareRepository.save(meta);
            fileStorageService.storeFile(savedEntity.getId(), file);
        } catch (final Exception e) {
            log.error("Cannot store file", e);
            throw new StorageException("Cannot store file", e);
        }
    }

    @Transactional
    public void deleteFile(final UUID id) throws IOException {
        final FileMeta meta = shareRepository.findById(id).orElseThrow(NotFoundException::new);
        final UUID fileId = meta.getId();
        shareRepository.delete(meta);
        fileStorageService.deleteFile(fileId);
    }
}
