package de.haevn.worksuite.share;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareRepository extends JpaRepository<FileMeta, UUID> {

}