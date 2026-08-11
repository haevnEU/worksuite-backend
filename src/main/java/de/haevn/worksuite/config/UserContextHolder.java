package de.haevn.worksuite.config;

import java.util.Optional;

/**
 * Thread-local container for accessing the current {@link UserIntegrationContext}.
 *
 * <p>Provides static helper methods to bind and inspect user contextual state throughout the request
 * execution lifecycle.
 *
 * <p>Example usage:
 * <pre>{@code
 * try {
 *     UserContextHolder.setContext(context);
 *     UserIntegrationContext current = UserContextHolder.getRequiredContext();
 *     // perform operations with current.vcsToken()
 * } finally {
 *     UserContextHolder.clear();
 * }
 * }</pre>
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserIntegrationContext> CONTEXT = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private UserContextHolder() {
    }

    /**
     * Retrieves the {@link UserIntegrationContext} associated with the current thread.
     *
     * @return the active context, or {@code null} if none is set
     */
    public static UserIntegrationContext getContext() {
        return CONTEXT.get();
    }

    /**
     * Binds the given {@link UserIntegrationContext} to the current thread.
     *
     * @param context the user context to store
     */
    public static void setContext(final UserIntegrationContext context) {
        CONTEXT.set(context);
    }

    /**
     * Retrieves the {@link UserIntegrationContext} wrapped in an {@link Optional}.
     *
     * @return an {@link Optional} containing the active context, or empty if unset
     */
    public static Optional<UserIntegrationContext> getOptionalContext() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * Retrieves the {@link UserIntegrationContext}, throwing an exception if unavailable.
     *
     * @return the active {@link UserIntegrationContext}
     * @throws IllegalStateException if no user integration context is bound to the current thread
     */
    public static UserIntegrationContext getRequiredContext() {
        return getOptionalContext().orElseThrow(
            () -> new IllegalStateException("No UserIntegrationContext found on the current thread."));
    }

    /**
     * Removes the {@link UserIntegrationContext} from the current thread to prevent thread leakage.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}