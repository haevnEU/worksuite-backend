package de.haevn.worksuite.share;

import de.haevn.worksuite.common.exceptions.BadRequestException;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidation {
    public boolean isPasswordValid(final ProtectedResource resource, final String password) {
        if (resource.getPassword() == null || resource.getPassword().isEmpty()) {
            return true;
        } else if (password == null || password.isEmpty()) {
            return false;
        }
        return resource.getPassword().equals(password);
    }


    public boolean isPasswordValid(final ProtectedResource resource, final Optional<String> password) {
        return isPasswordValid(resource, password.orElse(null));
    }

    public void validatePassword(final ProtectedResource resource, final Optional<String> password) {
        if (isPasswordValid(resource, password)) {
            return;
        }
        throw new BadRequestException("Password is not valid");
    }
}
