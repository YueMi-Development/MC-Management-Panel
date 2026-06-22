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
    public @NotNull CompletableFuture<Void> createBackup(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            EconomyProvider econ = getEconomyProvider();
            if (econ == null) {
                return;
            }

            OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);

            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            if (backupDir.exists()) {
                try {
                    double balance = econ.getBalance(op);
                    File economyFile = new File(backupDir, "economy_yuemi.txt");
                    Files.write(economyFile.toPath(), String.valueOf(balance).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to backup YueMi economy for " + playerId + ": " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> executeWipe(@NotNull UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            EconomyProvider econ = getEconomyProvider();
            if (econ == null) {
                return;
            }

            OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);

            try {
                econ.setBalance(op, 0.0);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to wipe YueMi economy for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> executeRestore(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            File economyFile = new File(backupDir, "economy_yuemi.txt");

            if (!economyFile.exists()) {
                return;
            }

            EconomyProvider econ = getEconomyProvider();
            if (econ == null) {
                return;
            }

            OfflinePlayer op = Bukkit.getOfflinePlayer(playerId);

            try {
                String balanceStr = new String(Files.readAllBytes(economyFile.toPath()), StandardCharsets.UTF_8);
                double balance = Double.parseDouble(balanceStr.trim());
                econ.setBalance(op, balance);
                plugin.getLogger().info("Restored YueMi economy for " + playerId);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to restore YueMi economy for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
