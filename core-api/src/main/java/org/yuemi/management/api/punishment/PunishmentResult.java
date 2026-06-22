package org.yuemi.management.api.punishment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a punishment operation.
 *
 * @param success  whether the operation succeeded
 * @param message  descriptive result or error message
 * @param actionId optional action reference/ID (e.g. database ID from the third-party plugin)
 */
public record PunishmentResult(
        boolean success,
        @NotNull String message,
        @Nullable String actionId
) {

    public static @NotNull PunishmentResult success(@NotNull String message) {
        return new PunishmentResult(true, message, null);
    }

    public static @NotNull PunishmentResult success(@NotNull String message, @Nullable String actionId) {
        return new PunishmentResult(true, message, actionId);
    }

    public static @NotNull PunishmentResult failure(@NotNull String message) {
        return new PunishmentResult(false, message, null);
    }
}
