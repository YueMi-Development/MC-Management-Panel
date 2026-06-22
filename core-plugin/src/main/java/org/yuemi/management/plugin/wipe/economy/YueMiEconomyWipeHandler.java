package org.yuemi.management.plugin.wipe.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.libs.api.YueMiLibsProvider;
import org.yuemi.libs.api.economy.EconomyProvider;
import org.yuemi.management.api.wipe.WipeHandler;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class YueMiEconomyWipeHandler implements WipeHandler {

    private final ManagementPanelPlugin plugin;

    public YueMiEconomyWipeHandler(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "yuemi";
    }

    private @Nullable EconomyProvider getEconomyProvider() {
        try {
            var api = YueMiLibsProvider.getApi();
            if (api != null && api.getEconomy() != null) {
                return api.getEconomy().getActiveProvider();
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("YueMiLibs economy provider is not available: " + t.getMessage());
        }
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> handleWipe(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            EconomyProvider econ = getEconomyProvider();
            if (econ == null) {
                return;
            }

            OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
            double balance = econ.getBalance(op);

            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            File backupFile = new File(backupDir, "economy.txt");

            try {
                // Back up balance if backup dir exists
                if (backupDir.exists()) {
                    Files.writeString(backupFile.toPath(), String.valueOf(balance), StandardCharsets.UTF_8);
                }

                // Wiping balance: set to 0.0
                econ.setBalance(op, 0.0);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to backup/wipe economy balance for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> handleUnwipe(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            EconomyProvider econ = getEconomyProvider();
            if (econ == null) {
                return;
            }

            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            File backupFile = new File(backupDir, "economy.txt");

            if (!backupFile.exists()) {
                return;
            }

            try {
                String content = Files.readString(backupFile.toPath(), StandardCharsets.UTF_8);
                double balance = Double.parseDouble(content.trim());
                OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);
                
                econ.setBalance(op, balance);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to restore economy balance for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
