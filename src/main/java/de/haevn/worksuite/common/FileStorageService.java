package de.haevn.worksuite.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>Service for storing and retrieving files.</p>
 * <p>This service provides a capsulated view on the file system, allowing for storing and retrieving files in a structured manner.</p>
 * <p>Files are stored in a directory structure based on the module name and file ID. The root directory for storage is "/data".</p>
 * <p>Example usage:</p>
 * {@snippet *FileStorageService fileStorageService = new FileStorageService("myModule");
     *UUID fileId = UUID.randomUUID();
     *MultipartFile file =...;//Obtain a MultipartFile from a request
 *fileStorageService.storeFile(fileId,file);
     *Resource resource = fileStorageService.loadFile(fileId.toString());
     *fileStorageService.deleteFile(fileId);
 *}
 */
public class FileStorageService {
    private final Path rootPath = Path.of("/data");
    private final String moduleName;

    /**
     * Initializes the service with the given module name
     * @param moduleName must not be null or empty
     */
    public FileStorageService(final String moduleName) {
        Objects.requireNonNull(moduleName, "Module name cannot be null");

        this.moduleName = moduleName;
    }

    /**
     * <p>Stores a {@link MultipartFile} on the disk, the filename is the provided {@link UUID id}.</p>
     * @param fileId must not be null
     * @param file must not be null
     * @return the ID of the stored file
     * @throws IOException if an I/O error occurs
     */
    public String storeFile(final UUID fileId, final MultipartFile file) throws IOException {
        Objects.requireNonNull(fileId, "File id cannot be null");
        Objects.requireNonNull(file, "File cannot be null");
        final Path targetPath = resolvePath(moduleName, fileId.toString());

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileId.toString();
    }

    /**
     * <p>Stores a string content as a file on the disk, the filename is the provided {@link UUID id}.</p>
     * @param fileId must not be null
     * @param content must not be null
     * @return the ID of the stored file
     * @throws IOException if an I/O error occurs
     */
    public String storeFile(final UUID fileId, final String content) throws IOException {
        Objects.requireNonNull(fileId, "File id cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
        return storeFile(fileId, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * <p>Stores a byte array as a file on the disk, the filename is the provided {@link UUID id}.</p>
     * @param fileId must not be null
     * @param content must not be null
     * @return the ID of the stored file
     * @throws IOException if an I/O error occurs
     */
    public String storeFile(final UUID fileId, final byte[] content) throws IOException {
        Objects.requireNonNull(fileId, "File id cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
        final Path targetPath = resolvePath(moduleName, fileId.toString());
        Files.write(targetPath, content);
        return fileId.toString();
    }

    /**
     * <p>Loads a file from the disk as a {@link Resource}.</p>
     * @param fileName must not be null
     * @return the loaded file as a {@link Resource}
     * @throws IOException if an I/O error occurs
     */
    public Resource loadFile(final String fileName) throws IOException {
        Objects.requireNonNull(fileName, "File name cannot be null");
        final Path filePath = resolvePath(moduleName, fileName);
        final Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Could not read file: " + fileName);
        }

        return resource;
    }

    /**
     * <p>Deletes a file from the disk.</p>
     * @param fileId must not be null
     * @throws IOException if an I/O error occurs
     */
    public void deleteFile(final UUID fileId) throws IOException {
        Objects.requireNonNull(fileId, "File id cannot be null");
        final Path filePath = resolvePath(moduleName, fileId.toString());
        Files.deleteIfExists(filePath);
    }

    /**
     * <p>Resolves the path for a given module name and file name, ensuring that the path is within the module's directory.</p>
     * @param moduleName must not be null
     * @param fileName must not be null
     * @return Resolved {@link Path} for the file
     * @throws IOException if an I/O error occurs
     */
    private Path resolvePath(final String moduleName, final String fileName) throws IOException {
        Objects.requireNonNull(moduleName, "Module name cannot be null");
        Objects.requireNonNull(fileName, "File name cannot be null");

        final Path modulePath = this.rootPath.resolve(moduleName).normalize();

        if (!Files.exists(modulePath)) {
            Files.createDirectories(modulePath);
        }

        final Path targetPath = modulePath.resolve(fileName).normalize();
        if (!targetPath.startsWith(modulePath)) {
            throw new SecurityException("Access denied: File path is outside the module directory.");
        }

        return targetPath;
    }

    public String calculateChecksum(final UUID id) {
        try {
            final Path filePath = resolvePath(moduleName, id.toString());
            final byte[] fileBytes = Files.readAllBytes(filePath);
            return java.util.Base64.getEncoder()
                .encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(fileBytes));
        } catch (Exception e) {
            throw new RuntimeException("Could not calculate checksum for file: " + id, e);
        }
    }

    public String calculateChecksum(final MultipartFile file) {
        try (final InputStream inputStream = file.getInputStream()) {
            return DigestUtils.sha256Hex(inputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Fehler beim Berechnen der Prüfsumme", e);
        }
    }
}