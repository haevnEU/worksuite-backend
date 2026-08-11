package de.haevn.worksuite.share;

import de.haevn.worksuite.common.FileStorageService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.common.exceptions.StorageException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service managing shared file storage, metadata tracking, and secure downloads.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private ShareService shareService;
 *
 * shareService.share(List.of(multipartFile));
 * ResponseEntity<Resource> download = shareService.downloadFile(fileId);
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ShareService {

    private final FileStorageService fileStorageService;
    private final ShareRepository shareRepository;

    /**
     * Uploads and stores a batch of multipart files.
     *
     * @param files the list of files to store
     */
    public void share(final List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        log.info("Storing batch of {} shared files", files.size());
        files.forEach(this::storeFile);
    }

    /**
     * Retrieves all active shared file metadata records.
     *
     * @return list of non-deleted {@link FileMeta} objects
     */
    @Transactional(readOnly = true)
    public List<FileMeta> getAllSharedFiles() {
        return shareRepository.findAllByDeletedFalse();
    }

    /**
     * Streams a shared file resource by ID with configured download headers.
     *
     * @param id unique file identifier
     * @return a {@link ResponseEntity} holding the streaming {@link Resource}
     * @throws IOException if loading the file fails
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadFile(final UUID id) throws IOException {
        Objects.requireNonNull(id, "File ID must not be null");

        final FileMeta meta = findFileMetaEntity(id);
        final Resource resource = fileStorageService.loadFile(id.toString());

        return createDownloadResponse(meta, resource);
    }

    /**
     * Persists metadata and binary contents for a single {@link MultipartFile}.
     *
     * @param file the upload payload
     * @throws StorageException if file storage or checksum computation fails
     */
    @Transactional
    public void storeFile(final MultipartFile file) {
        Objects.requireNonNull(file, "MultipartFile must not be null");

        try {
            final String checksum = fileStorageService.calculateChecksum(file);
            final FileMeta meta =
                FileMeta.builder().filename(file.getOriginalFilename()).fileType(resolveContentType(file))
                    .fileSize(file.getSize()).checksum(checksum).build();

            final FileMeta savedEntity = shareRepository.save(meta);
            fileStorageService.storeFile(savedEntity.getId(), file);
            log.info("Stored file '{}' with ID: '{}'", savedEntity.getFilename(), savedEntity.getId());
        } catch (final Exception ex) {
            log.error("Failed to store uploaded file '{}'", file.getOriginalFilename(), ex);
            throw new StorageException("Cannot store file: " + file.getOriginalFilename(), ex);
        }
    }

    /**
     * Deletes a shared file entity and removes its physical file from storage.
     *
     * @param id unique file identifier
     * @throws IOException if physical deletion fails
     */
    @Transactional
    public void deleteFile(final UUID id) throws IOException {
        Objects.requireNonNull(id, "File ID must not be null");

        final FileMeta meta = findFileMetaEntity(id);
        shareRepository.delete(meta);
        fileStorageService.deleteFile(id);
        log.info("Deleted shared file with ID: '{}'", id);
    }

    /**
     * Resolves the {@link FileMeta} entity by ID or throws {@link NotFoundException}.
     *
     * <p>Example usage:
     * <pre>{@code
     * FileMeta meta = findFileMetaEntity(id);
     * }</pre>
     *
     * @param id target file identifier
     * @return the persistent {@link FileMeta} entity
     * @throws NotFoundException if the file is not found or marked deleted
     */
    private FileMeta findFileMetaEntity(final UUID id) {
        return shareRepository.findById(id).filter(meta -> !meta.isDeleted()).orElseThrow(NotFoundException::new);
    }

    /**
     * Safely determines the content type of a multipart file.
     *
     * <p>Example usage:
     * <pre>{@code
     * String type = resolveContentType(multipartFile);
     * }</pre>
     *
     * @param file the multipart file
     * @return MIME content type string, defaulting to {@code "application/octet-stream"}
     */
    private String resolveContentType(final MultipartFile file) {
        return file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    /**
     * Constructs a {@link ResponseEntity} with appropriate attachment headers.
     *
     * <p>Example usage:
     * <pre>{@code
     * return createDownloadResponse(meta, resource);
     * }</pre>
     *
     * @param meta the file metadata
     * @param resource the binary resource
     * @return configured {@link ResponseEntity}
     */
    private ResponseEntity<Resource> createDownloadResponse(final FileMeta meta, final Resource resource) {
        final ContentDisposition disposition =
            ContentDisposition.attachment().filename(meta.getFilename(), StandardCharsets.UTF_8).build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .contentType(MediaType.parseMediaType(meta.getFileType())).contentLength(meta.getFileSize()).body(resource);
    }
}