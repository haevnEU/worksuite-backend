package de.haevn.worksuite.share.file;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileShareDTO(UUID id, String originalFilename, String contentType, long fileSizeBytes, String storagePath,
                           String password, int downloadCount, int maxDownloads, OffsetDateTime createdAt,
                           OffsetDateTime expiresAt) {

    public static FileShareDTO fromModel(final FileModel model) {
        if (model == null) {
            return null;
        }

        return new FileShareDTO(model.getId(), model.getOriginalFilename(), model.getContentType(),
            model.getFileSizeBytes(), model.getStoragePath(), "<hidden>", // Passwort nicht nach außen geben
            model.getDownloadCount(), model.getMaxDownloads(), model.getCreatedAt(), model.getExpiresAt());
    }

    public FileModel toModel() {
        final FileModel model = new FileModel();
        model.setId(this.id);
        model.setOriginalFilename(this.originalFilename);
        model.setContentType(this.contentType);
        model.setFileSizeBytes(this.fileSizeBytes);
        model.setStoragePath(this.storagePath);
        model.setPassword(this.password);
        model.setDownloadCount(this.downloadCount);
        model.setMaxDownloads(this.maxDownloads);
        model.setCreatedAt(this.createdAt);
        model.setExpiresAt(this.expiresAt);
        return model;
    }
}