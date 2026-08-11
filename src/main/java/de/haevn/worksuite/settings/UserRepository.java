package de.haevn.worksuite.settings;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing database operations for {@link UserModel} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private UserRepository userRepository;
 *
 * Optional<UserModel> user = userRepository.findById(UUID.randomUUID());
 * }</pre>
 */
@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {
}