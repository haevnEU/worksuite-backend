package de.haevn.worksuite.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for local file system storage with path-traversal protection and SHA-256 checksum hashing.
 *
 * <p>Example usage:
 * <pre>{@code
 * FileStorageService storageService = new FileStorageService("invoices");
 * UUID fileId = UUID.randomUUID();
 *
 * storageService.storeFile(fileId, "Sample invoice body content");
 * String checksum = storageService.calculateChecksum(fileId);
 * Resource fileResource = storageService.loadFile(fileId.toString());
 * storageService.deleteFile(fileId);
 * }</pre>
 */
public class FileStorageService {

    private static final Path DEFAULT_ROOT = Path.of("/data");
    private static final String HASH_ALGORITHM = "SHA-256";

    private final Path modulePath;

    /**
     * Initializes storage rooted at {@code /data/{moduleName}}.
     *
     * @param moduleName the sub-directory identifier; must not be null or blank
     */
    public FileStorageService(final String moduleName) {
        this(DEFAULT_ROOT, moduleName);
    }

    /**
     * Initializes storage with a custom root directory and module identifier.
     *
     * @param rootDir the parent root path
     * @param moduleName the sub-directory identifier
     */
    public FileStorageService(final Path rootDir, final String moduleName) {
        Objects.requireNonNull(rootDir, "Root directory must not be null");
        Objects.requireNonNull(moduleName, "Module name must not be null");
        if (moduleName.isBlank()) {
            throw new IllegalArgumentException("Module name must not be blank");
        }
        this.modulePath = rootDir.resolve(moduleName).normalize();
    }

    /**
     * Streams input data through a {@link MessageDigest} and formats the result as a lowercase hex string.
     *
     * <p>Processes the stream in 8 KB chunks to minimize memory overhead for large files.
     *
     * <p>Example usage:
     * <pre>{@code
     * try (InputStream is = Files.newInputStream(Path.of("/data/archive.zip"))) {
     *     String hexHash = digestStreamToHex(is);
     * }
     * }</pre>
     *
     * @param inputStream the {@link InputStream} providing the binary data to hash
     * @return a hex-encoded SHA-256 digest string
     * @throws IOException if reading from the input stream fails
     * @throws NoSuchAlgorithmException if the underlying cryptographic algorithm is not supported
     */
    private static String digestStreamToHex(final InputStream inputStream)
        throws IOException, NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        final byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Stores the contents of a {@link MultipartFile} using a {@link UUID} as the filename.
     *
     * @param fileId the unique file identifier
     * @param file the multipart upload payload
     * @return the string identifier of the saved file
     * @throws IOException if an I/O error occurs during write
     */
    public String storeFile(final UUID fileId, final MultipartFile file) throws IOException {
        Objects.requireNonNull(fileId, "File ID must not be null");
        Objects.requireNonNull(file, "File must not be null");

        final Path targetPath = resolvePath(fileId.toString());
        try (final InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileId.toString();
    }

    /**
     * Stores string content encoded as UTF-8.
     *
     * @param fileId the unique file identifier
     * @param content text content to write
     * @return the string identifier of the saved file
     * @throws IOException if an I/O error occurs
     */
    public String storeFile(final UUID fileId, final String content) throws IOException {
        Objects.requireNonNull(content, "Content must not be null");
        return storeFile(fileId, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Stores a raw byte array.
     *
     * @param fileId the unique file identifier
     * @param content binary content to write
     * @return the string identifier of the saved file
     * @throws IOException if an I/O error occurs
     */
    public String storeFile(final UUID fileId, final byte[] content) throws IOException {
        Objects.requireNonNull(fileId, "File ID must not be null");
        Objects.requireNonNull(content, "Content must not be null");

        final Path targetPath = resolvePath(fileId.toString());
        Files.write(targetPath, content);
        return fileId.toString();
    }

    /**
     * Loads a file as a readable {@link Resource}.
     *
     * @param fileName target filename to resolve
     * @return the readable {@link Resource}
     * @throws IOException if the file does not exist or is unreadable
     */
    public Resource loadFile(final String fileName) throws IOException {
        Objects.requireNonNull(fileName, "File name must not be null");

        final Path filePath = resolvePath(fileName);
        final Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Could not read file: " + fileName);
        }
        return resource;
    }

    /**
     * Deletes a file if it exists.
     *
     * @param fileId unique file identifier
     * @throws IOException if deletion fails
     */
    public void deleteFile(final UUID fileId) throws IOException {
        Objects.requireNonNull(fileId, "File ID must not be null");
        Files.deleteIfExists(resolvePath(fileId.toString()));
    }

    /**
     * Calculates the hex-encoded SHA-256 checksum for a stored file by ID.
     *
     * @param id unique file identifier
     * @return lower-case hex SHA-256 checksum
     */
    public String calculateChecksum(final UUID id) {
        Objects.requireNonNull(id, "File ID must not be null");
        try (final InputStream inputStream = Files.newInputStream(resolvePath(id.toString()))) {
            return digestStreamToHex(inputStream);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not calculate checksum for file ID: " + id, ex);
        }
    }

    /**
     * Calculates the hex-encoded SHA-256 checksum for a {@link MultipartFile}.
     *
     * @param file the multipart upload to inspect
     * @return lower-case hex SHA-256 checksum
     */
    public String calculateChecksum(final MultipartFile file) {
        Objects.requireNonNull(file, "MultipartFile must not be null");
        try (final InputStream inputStream = file.getInputStream()) {
            return digestStreamToHex(inputStream);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not calculate checksum for uploaded file", ex);
        }
    }

    /**
     * Resolves a file name within the module directory while ensuring path-traversal safety.
     *
     * <p>Creates the module directory if it does not already exist and verifies that the resulting
     * {@link Path} does not escape the configured root boundary.
     *
     * <p>Example usage:
     * <pre>{@code
     * Path destination = resolvePath("invoice_2026.pdf");
     * Files.write(destination, fileBytes);
     * }</pre>
     *
     * @param fileName the name or relative path of the file to resolve
     * @return the normalized, validated {@link Path} pointing inside the module directory
     * @throws IOException if creating directories fails
     * @throws SecurityException if path traversal outside the module directory is detected
     */
    private Path resolvePath(final String fileName) throws IOException {
        Objects.requireNonNull(fileName, "File name must not be null");

        if (!Files.exists(modulePath)) {
            Files.createDirectories(modulePath);
        }

        final Path targetPath = modulePath.resolve(fileName).normalize();
        if (!targetPath.startsWith(modulePath)) {
            throw new SecurityException("Access denied: Path traversal outside module directory detected.");
        }
        return targetPath;
    }
}