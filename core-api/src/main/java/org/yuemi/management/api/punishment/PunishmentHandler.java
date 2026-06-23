package org.yuemi.management.api.punishment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface representing a punishment adapter (e.g. EssentialsX, Vanilla).
 */
public interface PunishmentHandler {

    /**
     * Gets the name of the punishment handler.
     *
     * @return the handler name
     */
    @NotNull String getName();

    /**
     * Bans a player.
     *
     * @param playerId the target player's UUID
     * @param reason   the ban reason
     * @param duration the duration of the ban (null for permanent)
     * @param source   the source/sender of the ban
     * @return a future resolving to the PunishmentResult
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
     * @param playerId the target player's UUID
     * @param reason   the unban reason
     * @param source   the source/sender of the unban
     * @return a future resolving to the PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> unban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    );

    /**
     * Mutes a player.
     *
     * @param playerId the target player's UUID
     * @param reason   the mute reason
     * @param duration the duration of the mute (null for permanent)
     * @param source   the source/sender of the mute
     * @return a future resolving to the PunishmentResult
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
     * @param playerId the target player's UUID
     * @param reason   the unmute reason
     * @param source   the source/sender of the unmute
     * @return a future resolving to the PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> unmute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    );

    /**
     * Kicks a player.
     *
     * @param playerId the target player's UUID
     * @param reason   the kick reason
     * @param source   the source/sender of the kick
     * @return a future resolving to the PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> kick(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    );

    /**
     * Warns a player.
     *
     * @param playerId the target player's UUID
     * @param reason   the warning reason
     * @param source   the source/sender of the warning
     * @return a future resolving to the PunishmentResult
     */
    @NotNull CompletableFuture<PunishmentResult> warn(
            @NotNull UUID playerId,
            @NotNull String reason,
            @Nullable String source
    );

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
