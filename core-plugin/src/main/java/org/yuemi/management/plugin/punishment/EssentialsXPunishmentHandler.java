package org.yuemi.management.plugin.punishment;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentHandler;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class EssentialsXPunishmentHandler implements PunishmentHandler {

    private final ManagementPanelPlugin plugin;

    public EssentialsXPunishmentHandler(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "essentials";
    }

    private @NotNull String getTargetIdentifier(@NotNull UUID playerId) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
        String name = op.getName();
        return name != null ? name : playerId.toString();
    }

    private @NotNull String formatDuration(@NotNull Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds <= 0) return "1s";
        if (seconds % 86400 == 0) {
            return (seconds / 86400) + "d";
        } else if (seconds % 3600 == 0) {
            return (seconds / 3600) + "h";
        } else if (seconds % 60 == 0) {
            return (seconds / 60) + "m";
        }
        return seconds + "s";
    }

    private @NotNull CompletableFuture<PunishmentResult> executeConsoleCommand(@NotNull String command) {
        CompletableFuture<PunishmentResult> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                if (success) {
                    future.complete(PunishmentResult.success("Essentials command executed successfully: /" + command));
                } else {
                    future.complete(PunishmentResult.failure("Failed to execute Essentials command: /" + command));
                }
            } catch (Exception e) {
                future.complete(PunishmentResult.failure("Error executing Essentials command: " + e.getMessage()));
            }
        });
        return future;
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> ban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    ) {
        String target = getTargetIdentifier(playerId);
        String reasonStr = reason != null && !reason.isBlank() ? " " + reason : "";
        
        if (duration != null) {
            String time = formatDuration(duration);
            return executeConsoleCommand("essentials:tempban " + target + " " + time + reasonStr);
        } else {
            return executeConsoleCommand("essentials:ban " + target + reasonStr);
        }
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> unban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        String target = getTargetIdentifier(playerId);
        return executeConsoleCommand("essentials:unban " + target);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> mute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    ) {
        String target = getTargetIdentifier(playerId);
        String time = duration != null ? " " + formatDuration(duration) : "";
        
        // Essentials mute takes reason as last parameter: /mute <player> [duration]
        // Actually, EssentialsX mute is /mute <player> [duration]
        return executeConsoleCommand("essentials:mute " + target + time);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> unmute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        String target = getTargetIdentifier(playerId);
        return executeConsoleCommand("essentials:mute " + target + " off");
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> kick(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        String target = getTargetIdentifier(playerId);
        String reasonStr = reason != null && !reason.isBlank() ? " " + reason : "";
        return executeConsoleCommand("essentials:kick " + target + reasonStr);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> warn(
            @NotNull UUID playerId,
            @NotNull String reason,
            @Nullable String source
    ) {
        // EssentialsX does not have a native warn command by default, 
        // but we can try running it as a generic fallback.
        String target = getTargetIdentifier(playerId);
        return executeConsoleCommand("warn " + target + " " + reason);
    }
}
