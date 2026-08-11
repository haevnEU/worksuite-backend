package de.haevn.worksuite.share;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing metadata for a shared file stored in the workspace file system.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * FileMeta meta = FileMeta.builder()
 *     .filename("database_schema.sql")
 *     .fileType("text/plain")
 *     .fileSize(4096L)
 *     .checksum("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_meta")
@Schema(description = "Metadata descriptor for a shared file upload")
public class FileMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "Unique file identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID id;

    @Column(name = "filename", nullable = false)
    @Schema(description = "Original filename", example = "docker-compose.yml")
    private String filename;

    @Column(name = "file_type", nullable = false)
    @Schema(description = "MIME content type", example = "application/x-yaml")
    private String fileType;

    @Column(name = "file_size", nullable = false)
    @Schema(description = "File size in bytes", example = "2048")
    private Long fileSize;

    @Column(name = "checksum", nullable = false)
    @Schema(description = "SHA-256 hex checksum",
        example = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e")
    private String checksum;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Upload timestamp", example = "2026-08-17T18:55:00")
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    @Schema(description = "Soft deletion status", example = "false")
    private boolean deleted = false;

    /**
     * Pre-persist lifecycle callback initializing the creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}