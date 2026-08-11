package de.haevn.worksuite.share.file;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileShareRepository extends JpaRepository<FileModel, UUID> {
    List<FileModel> findByExpiresAtBefore(final OffsetDateTime now);

    @Modifying
    @Query("UPDATE FileModel f SET f.downloadCount = f.downloadCount + 1 WHERE f.id = :id")
    void incrementDownloadCount(@Param("id") final UUID id);

    List<FileModel> findByOriginalFilenameContainingIgnoreCase(final String filename);
}