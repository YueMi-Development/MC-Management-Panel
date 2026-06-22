package org.yuemi.management.plugin.wipe;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.wipe.WipeHandler;
import org.yuemi.management.api.wipe.WipeService;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import org.yuemi.management.plugin.wipe.inventory.VanillaInventoryWipeHandler;
import org.yuemi.management.plugin.wipe.economy.EssentialsXEconomyWipeHandler;
import org.yuemi.management.plugin.wipe.economy.YueMiEconomyWipeHandler;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class WipeServiceImpl implements WipeService {

    private final ManagementPanelPlugin plugin;
    private final List<WipeHandler> handlers = new ArrayList<>();
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public WipeServiceImpl(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
        registerDefaultHandlers();
    }

    private void registerDefaultHandlers() {
        String inventoryHandler = plugin.getConfig().getString("wipe.handlers.inventory", "vanilla");
        if ("vanilla".equalsIgnoreCase(inventoryHandler)) {
            registerHandler(new VanillaInventoryWipeHandler(plugin));
        }

        String economyHandler = plugin.getConfig().getString("wipe.handlers.economy", "yuemi");
        if ("yuemi".equalsIgnoreCase(economyHandler) && Bukkit.getPluginManager().getPlugin("YueMiLibs") != null) {
            registerHandler(new YueMiEconomyWipeHandler(plugin));
        } else if ("essentials".equalsIgnoreCase(economyHandler) && Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            registerHandler(new EssentialsXEconomyWipeHandler(plugin));
        }
    }

    public void reload() {
        handlers.clear();
        registerDefaultHandlers();
    }

    @Override
    public @NotNull CompletableFuture<String> wipe(@NotNull UUID playerId) {
        String backupId = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        CompletableFuture<String> future = new CompletableFuture<>();

        // Ensure player is kicked on the main thread first to avoid save overwriting
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.kick(net.kyori.adventure.text.Component.text("Your player profile is being wiped."));
            }

            // Create backup directory if backups are enabled
            if (plugin.getConfig().getBoolean("wipe.enable-backups", true)) {
                File backupDir = getBackupDirectory(playerId, backupId);
                if (!backupDir.exists() && !backupDir.mkdirs()) {
                    future.completeExceptionally(new SecurityException("Failed to create backup directory: " + backupDir.getPath()));
                    return;
                }
            }

            // Run handlers asynchronously
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (WipeHandler handler : handlers) {
                futures.add(handler.handleWipe(playerId, backupId));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> future.complete(backupId))
                    .exceptionally(throwable -> {
                        future.completeExceptionally(throwable);
                        return null;
                    });
        });

        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> unwipe(@NotNull UUID playerId, @Nullable String backupId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String targetBackupId = backupId;
            if (targetBackupId == null) {
                targetBackupId = findLatestBackupId(playerId);
                if (targetBackupId == null) {
                    future.completeExceptionally(new NoSuchElementException("No backups found to restore for player: " + playerId));
                    return;
                }
            }

            final String finalBackupId = targetBackupId;
            File backupDir = getBackupDirectory(playerId, finalBackupId);
            if (!backupDir.exists()) {
                future.completeExceptionally(new NoSuchElementException("Backup directory does not exist: " + backupDir.getPath()));
                return;
            }

            // Run handlers to restore
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (WipeHandler handler : handlers) {
                futures.add(handler.handleUnwipe(playerId, finalBackupId));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> future.complete(null))
                    .exceptionally(throwable -> {
                        future.completeExceptionally(throwable);
                        return null;
                    });
        });

        return future;
    }

    @Override
    public void registerHandler(@NotNull WipeHandler handler) {
        handlers.add(handler);
        plugin.getLogger().info("Registered wipe handler: " + handler.getName());
    }

    @Override
    public void unregisterHandler(@NotNull WipeHandler handler) {
        handlers.remove(handler);
        plugin.getLogger().info("Unregistered wipe handler: " + handler.getName());
    }

    @Override
    public @NotNull List<WipeHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    public @NotNull File getBackupDirectory(@NotNull UUID playerId, @NotNull String backupId) {
        String baseDirName = plugin.getConfig().getString("wipe.backup-directory", "backups");
        File baseDir = new File(plugin.getDataFolder(), baseDirName);
        File playerDir = new File(baseDir, playerId.toString());
        return new File(playerDir, backupId);
    }

    private @Nullable String findLatestBackupId(@NotNull UUID playerId) {
        String baseDirName = plugin.getConfig().getString("wipe.backup-directory", "backups");
        File baseDir = new File(plugin.getDataFolder(), baseDirName);
        File playerDir = new File(baseDir, playerId.toString());
        
        if (!playerDir.exists() || !playerDir.isDirectory()) {
            return null;
        }

        File[] files = playerDir.listFiles(File::isDirectory);
        if (files == null || files.length == 0) {
            return null;
        }

        // Sort folders alphabetically (timestamp pattern naturally sorts correctly)
        Arrays.sort(files, Comparator.comparing(File::getName));
        return files[files.length - 1].getName();
    }
}
