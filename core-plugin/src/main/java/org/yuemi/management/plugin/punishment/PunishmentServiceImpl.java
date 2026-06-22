package org.yuemi.management.plugin.punishment;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentHandler;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.api.punishment.PunishmentService;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PunishmentServiceImpl implements PunishmentService {

    private final ManagementPanelPlugin plugin;
    private final Map<String, PunishmentHandler> handlers = new HashMap<>();
    private PunishmentHandler activeHandler;

    public PunishmentServiceImpl(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
        registerDefaultHandlers();
        resolveActiveHandler();
    }

    private void registerDefaultHandlers() {
        registerHandler(new VanillaPunishmentHandler());
        registerHandler(new EssentialsXPunishmentHandler(plugin));
    }

    private void resolveActiveHandler() {
        String configured = plugin.getConfig().getString("punishment-handler", "vanilla").toLowerCase();
        PunishmentHandler handler = handlers.get(configured);

        if (handler == null) {
            plugin.getLogger().warning("Configured punishment-handler '" + configured + "' is not recognized. Falling back to 'vanilla'.");
            handler = handlers.get("vanilla");
        }

        // Validate integrations are loaded
        if (configured.equals("essentials") && Bukkit.getPluginManager().getPlugin("Essentials") == null) {
            plugin.getLogger().warning("EssentialsX plugin is not installed/loaded! Falling back to 'vanilla' punishment handler.");
            handler = handlers.get("vanilla");
        }

        this.activeHandler = handler;
        plugin.getLogger().info("Active punishment handler: " + activeHandler.getName());
    }

    public void reload() {
        resolveActiveHandler();
    }

    public void registerHandler(@NotNull PunishmentHandler handler) {
        handlers.put(handler.getName().toLowerCase(), handler);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> ban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    ) {
        return activeHandler.ban(playerId, reason, duration, source);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> unban(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        return activeHandler.unban(playerId, reason, source);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> mute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable Duration duration,
            @Nullable String source
    ) {
        return activeHandler.mute(playerId, reason, duration, source);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> unmute(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        return activeHandler.unmute(playerId, reason, source);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> kick(
            @NotNull UUID playerId,
            @Nullable String reason,
            @Nullable String source
    ) {
        return activeHandler.kick(playerId, reason, source);
    }

    @Override
    public @NotNull CompletableFuture<PunishmentResult> warn(
            @NotNull UUID playerId,
            @NotNull String reason,
            @Nullable String source
    ) {
        return activeHandler.warn(playerId, reason, source);
    }

    @Override
    public @NotNull PunishmentHandler getActiveHandler() {
        return activeHandler;
    }

    @Override
    public void setActiveHandler(@NotNull PunishmentHandler handler) {
        this.activeHandler = handler;
        plugin.getLogger().info("Active punishment handler overridden to: " + handler.getName());
    }
}
