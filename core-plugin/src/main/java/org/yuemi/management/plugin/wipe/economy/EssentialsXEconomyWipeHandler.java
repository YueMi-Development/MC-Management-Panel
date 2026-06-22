package org.yuemi.management.plugin.wipe.economy;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.yuemi.management.api.wipe.WipeHandler;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class EssentialsXEconomyWipeHandler implements WipeHandler {

    private final ManagementPanelPlugin plugin;

    public EssentialsXEconomyWipeHandler(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "essentials";
    }

    @Override
    public @NotNull CompletableFuture<Void> createBackup(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess == null) {
                return;
            }

            User user = ess.getUser(playerId);
            if (user == null) {
                return;
            }

            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            if (backupDir.exists()) {
                try {
                    BigDecimal balance = user.getMoney();
                    File essFile = new File(backupDir, "economy_essentials.txt");
                    Files.write(essFile.toPath(), balance.toString().getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to backup Essentials economy for " + playerId + ": " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> executeWipe(@NotNull UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess == null) {
                return;
            }

            User user = ess.getUser(playerId);
            if (user == null) {
                return;
            }

            try {
                user.setMoney(BigDecimal.ZERO);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to wipe Essentials economy for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> executeRestore(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            File essFile = new File(backupDir, "economy_essentials.txt");

            if (!essFile.exists()) {
                return;
            }

            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess == null) {
                return;
            }

            User user = ess.getUser(playerId);
            if (user == null) {
                return;
            }

            try {
                String balanceStr = new String(Files.readAllBytes(essFile.toPath()), StandardCharsets.UTF_8);
                BigDecimal balance = new BigDecimal(balanceStr.trim());
                user.setMoney(balance);
                plugin.getLogger().info("Restored Essentials economy for " + playerId);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to restore Essentials economy for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
