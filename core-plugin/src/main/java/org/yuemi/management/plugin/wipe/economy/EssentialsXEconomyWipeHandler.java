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
    public @NotNull CompletableFuture<Void> handleWipe(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess == null) {
                return;
            }

            User user = ess.getUser(playerId);
            if (user == null) {
                return;
            }

            BigDecimal balance = user.getMoney();

            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            File backupFile = new File(backupDir, "economy_essentials.txt");

            try {
                // Back up balance if backup dir exists
                if (backupDir.exists()) {
                    Files.writeString(backupFile.toPath(), balance.toString(), StandardCharsets.UTF_8);
                }

                // Wiping balance: set to 0
                user.setMoney(BigDecimal.ZERO);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to backup/wipe Essentials economy balance for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to wipe Essentials economy balance for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> handleUnwipe(@NotNull UUID playerId, @NotNull String backupId) {
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
            File backupFile = new File(backupDir, "economy_essentials.txt");

            if (!backupFile.exists()) {
                return;
            }

            try {
                String content = Files.readString(backupFile.toPath(), StandardCharsets.UTF_8);
                BigDecimal balance = new BigDecimal(content.trim());
                
                user.setMoney(balance);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to restore Essentials economy balance for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
