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
     * Performs a wipe action for the specified player.
     *
     * @param playerId the UUID of the player to wipe
     * @param backupId the unique backup ID generated for this wipe session
     * @return a future completing when the wipe is finished
     */
    @NotNull CompletableFuture<Void> handleWipe(@NotNull UUID playerId, @NotNull String backupId);

    /**
     * Restores the wiped data for the specified player from a backup.
     *
     * @param playerId the UUID of the player to unwipe
     * @param backupId the unique backup ID to restore from
     * @return a future completing when the unwipe/restore is finished
     */
    @NotNull CompletableFuture<Void> handleUnwipe(@NotNull UUID playerId, @NotNull String backupId);
}
