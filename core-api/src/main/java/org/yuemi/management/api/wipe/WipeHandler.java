package org.yuemi.management.api.wipe;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface representing a custom data wiper handler.
 */
public interface WipeHandler {

    /**
     * Gets the unique name of this wipe handler.
     *
     * @return the handler name
     */
    @NotNull String getName();

    /** 
     * Indicates if this handler supports creating backups. 
     * 
     * @return true if backups are supported, false otherwise
     */
    default boolean supportsBackup() { return true; }

    /** 
     * Indicates if this handler supports restoring from backups. 
     * 
     * @return true if restores are supported, false otherwise
     */
    default boolean supportsRestore() { return true; }

    /**
     * Generates a backup of the player's data.
     * Only called if backups are enabled globally and supportsBackup() is true.
     *
     * @param playerId the UUID of the player to backup
     * @param backupId the unique backup ID for this session
     * @return a future completing when the backup is finished
     */
    @NotNull CompletableFuture<Void> createBackup(@NotNull UUID playerId, @NotNull String backupId);

    /**
     * Executes the actual wipe action for the specified player.
     *
     * @param playerId the UUID of the player to wipe
     * @return a future completing when the wipe is finished
     */
    @NotNull CompletableFuture<Void> executeWipe(@NotNull UUID playerId);

    /**
     * Restores the wiped data for the specified player from a backup.
     * Only called if supportsRestore() is true.
     *
     * @param playerId the UUID of the player to unwipe
     * @param backupId the unique backup ID to restore from
     * @return a future completing when the unwipe/restore is finished
     */
    @NotNull CompletableFuture<Void> executeRestore(@NotNull UUID playerId, @NotNull String backupId);

    /**
     * Optional synchronous hook called on the main thread right before the player is kicked.
     * Useful for clearing in-memory data (like active inventory) to ensure it is saved empty.
     *
     * @param playerId the UUID of the player to wipe
     */
    default void preWipeSync(@NotNull UUID playerId) {}
}
