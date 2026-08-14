package de.haevn.worksuite.settings;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LicenseService {
    private final LicenseRepository licenseRepository;

    public License findById(final UUID id) {
        return licenseRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    public boolean licenseExpired(final UUID userId) {
        final License license = findById(userId);
        return license.isExpired();
    }
}
