package de.haevn.worksuite.settings;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing persistence for {@link License} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private LicenseRepository licenseRepository;
 *
 * Optional<License> license = licenseRepository.findById(userId);
 * }</pre>
 */
@Repository
public interface LicenseRepository extends JpaRepository<License, UUID> {
}