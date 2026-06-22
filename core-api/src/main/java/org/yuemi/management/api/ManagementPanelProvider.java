package org.yuemi.management.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Accessor class for the Management Panel API.
 */
public final class ManagementPanelProvider {

    private static ManagementPanelApi instance = null;

    private ManagementPanelProvider() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Gets the active API instance.
     *
     * @return the API instance
     * @throws IllegalStateException if the API is not yet initialized
     */
    public static @NotNull ManagementPanelApi get() {
        if (instance == null) {
            throw new IllegalStateException("ManagementPanel API is not initialized yet!");
        }
        return instance;
    }

    /**
     * Registers the API instance. Internal use only.
     *
     * @param api the API instance
     */
    @ApiStatus.Internal
    public static void register(@NotNull ManagementPanelApi api) {
        instance = api;
    }

    /**
     * Unregisters the API instance. Internal use only.
     */
    @ApiStatus.Internal
    public static void unregister() {
        instance = null;
    }
}
