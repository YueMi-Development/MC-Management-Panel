package org.yuemi.management.api.punishment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages punishment activities and coordinates with the active punishment integration.
 */
public interface PunishmentService {

    /**
     * Bans a player.
     *
     * @param playerId the player UUID
     * @param reason   the ban reason
     * @param duration the ban duration (null for permanent)
     * @param source   the sender source
     * @return future with PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> ban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    );

    /**
     * Unbans a player.
     *
     * @param playerId the player UUID
     * @param reason   the unban reason
     * @param source   the sender source
     * @return future with PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> unban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    );

    /**
     * Mutes a player.
     *
     * @param playerId the player UUID
     * @param reason   the mute reason
     * @param duration the mute duration (null for permanent)
     * @param source   the sender source
     * @return future with PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> mute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    );

    /**
     * Unmutes a player.
     *
     * @param playerId the player UUID
     * @param reason   the unmute reason
     * @param source   the sender source
     * @return future with PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> unmute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    );

    /**
     * Kicks a player.
     *
     * @param playerId the player UUID
     * @param reason   the kick reason
     * @param source   the sender source
     * @return future with PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> kick(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    );

    /**
     * Warns a player.
     *
     * @param playerId the player UUID
     * @param reason   the warning reason
     * @param source   the sender source
     * @return future with PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> warn(
            @NotNull UUID playerId,
            @NotNull String reason,
            @Nullable String source
    );

    /**
     * Gets the currently active punishment handler.
     *
     * @return the active handler
     */
    @NotNull PunishmentHandler getActiveHandler();

    /**
     * Sets the active punishment handler.
     *
     * @param handler the new punishment handler
     */
    void setActiveHandler(@NotNull PunishmentHandler handler);

    /**
     * Checks if a player is currently banned.
     *
     * @param playerId the player UUID
     * @return true if the player is banned, false otherwise
     */
    boolean isBanned(@NotNull UUID playerId);

    /**
     * Checks if a player is currently muted.
     *
     * @param playerId the player UUID
     * @return true if the player is muted, false otherwise
     */
    boolean isMuted(@NotNull UUID playerId);
}
