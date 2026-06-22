package org.yuemi.management.api.wipe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service to orchestrate backups, player wipes, and unwipes/restores.
 */
public interface WipeService {

    /**
     * Performs a full data wipe for the player.
     * Generates a backup automatically before executing any wipe handlers.
     *
     * @param playerId the UUID of the player to wipe
     * @return a future resolving to the backup ID string on success
     */
    @NotNull CompletableFuture<String> wipe(@NotNull UUID playerId);

    /**
     * Performs an unwipe (restoring player data from backup).
     *
     * @param playerId the UUID of the player to restore
     * @param backupId the unique backup ID/timestamp. If null, uses the latest backup.
     * @return a future completing when the restore is finished
     */
    @NotNull CompletableFuture<Void> unwipe(@NotNull UUID playerId, @Nullable String backupId);

    /**
     * Registers a custom wipe handler.
     *
     * @param handler the handler to register
     */
    void registerHandler(@NotNull WipeHandler handler);

    /**
     * Unregisters a custom wipe handler.
     *
     * @param handler the handler to unregister
     */
    void unregisterHandler(@NotNull WipeHandler handler);

    /**
     * Gets a list of all registered wipe handlers.
     *
     * @return the list of registered handlers
     */
    @NotNull List<WipeHandler> getHandlers();
}
