package de.haevn.worksuite.config;

public final class UserContextHolder {
    private static final ThreadLocal<UserIntegrationContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static UserIntegrationContext getContext() {
        return CONTEXT.get();
    }

    public static void setContext(UserIntegrationContext context) {
        CONTEXT.set(context);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}