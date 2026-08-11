package de.haevn.worksuite.settings;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user workspace licenses and checking subscription expiration.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private LicenseService licenseService;
 *
 * boolean expired = licenseService.licenseExpired(userId);
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    /**
     * Finds a {@link License} by the associated user identifier.
     *
     * @param userId the unique user identifier
     * @return the associated {@link License}
     * @throws NotFoundException if no license is registered for the user
     */
    @Transactional(readOnly = true)
    public License findById(final UUID userId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        return licenseRepository.findById(userId).orElseThrow(NotFoundException::new);
    }

    /**
     * Checks whether the user's license has expired.
     *
     * @param userId the unique user identifier
     * @return {@code true} if expired, {@code false} if valid
     */
    @Transactional(readOnly = true)
    public boolean licenseExpired(final UUID userId) {
        return findById(userId).isExpired();
    }
}