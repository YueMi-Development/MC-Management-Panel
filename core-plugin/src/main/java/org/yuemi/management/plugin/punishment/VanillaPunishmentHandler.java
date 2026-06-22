package org.yuemi.management.plugin.punishment;

import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentHandler;
import org.yuemi.management.api.punishment.PunishmentResult;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class VanillaPunishmentHandler implements PunishmentHandler {

    @Override
    public @NotNull String getName() {
        return "vanilla";
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull CompletableFuture<PunishmentResult> ban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerProfile profile = Bukkit.createProfile(playerId);
                Date dateLimit = duration != null ? new Date(System.currentTimeMillis() + duration.toMillis()) : null;
                
                // Fetch BanList for profile type
                @SuppressWarnings("unchecked")
                BanList<PlayerProfile> banList = (BanList<PlayerProfile>) Bukkit.getBanList(BanList.Type.PROFILE);
                banList.addBan(profile, reason, dateLimit, source);
                
                // Kick if currently online
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugins()[0], () -> {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        player.kick(Component.text(reason != null ? reason : "Banned by an administrator."));
                    }
                });

                return PunishmentResult.success("Player successfully banned using Vanilla API.");
            } catch (Exception e) {
                return PunishmentResult.failure("Failed to ban player: " + e.getMessage());
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> unban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerProfile profile = Bukkit.createProfile(playerId);
                @SuppressWarnings("unchecked")
                BanList<PlayerProfile> banList = (BanList<PlayerProfile>) Bukkit.getBanList(BanList.Type.PROFILE);
                banList.pardon(profile);
                return PunishmentResult.success("Player successfully unbanned using Vanilla API.");
            } catch (Exception e) {
                return PunishmentResult.failure("Failed to unban player: " + e.getMessage());
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> mute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    ) {
        return CompletableFuture.completedFuture(
                PunishmentResult.failure("Mutes are not supported by the Vanilla punishment system.")
        );
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> unmute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        return CompletableFuture.completedFuture(
                PunishmentResult.failure("Unmutes are not supported by the Vanilla punishment system.")
        );
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> kick(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        CompletableFuture<PunishmentResult> future = new CompletableFuture<>();
        // Kicking online players must be on main thread
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugins()[0], () -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.kick(Component.text(reason != null ? reason : "Kicked by an administrator."));
                future.complete(PunishmentResult.success("Player kicked."));
            } else {
                future.complete(PunishmentResult.failure("Player is not online."));
            }
        });
        return future;
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> warn(
            @NotNull UUID playerId,
            @NotNull String reason,
            @Nullable String source
    ) {
        return CompletableFuture.completedFuture(
                PunishmentResult.failure("Warnings are not supported by the Vanilla punishment system.")
        );
    }
}
