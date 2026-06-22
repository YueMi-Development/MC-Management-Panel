package org.yuemi.management.plugin.wipe;

import org.jetbrains.annotations.NotNull;
import org.yuemi.management.api.wipe.WipeHandler;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class EssentialsXWipeHandler implements WipeHandler {

    private final ManagementPanelPlugin plugin;

    public EssentialsXWipeHandler(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "essentials";
    }

    private File getEssentialsUserFile(@NotNull UUID playerId) {
        File essentialsDir = new File(plugin.getDataFolder().getParentFile(), "Essentials");
        File userdataDir = new File(essentialsDir, "userdata");
        return new File(userdataDir, playerId.toString() + ".yml");
    }

    private void copyFile(File source, File dest) throws IOException {
        if (!source.exists()) return;
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(dest)) {
            in.transferTo(out);
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> handleWipe(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            File userFile = getEssentialsUserFile(playerId);
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);

            try {
                if (userFile.exists() && backupDir.exists()) {
                    copyFile(userFile, new File(backupDir, "essentials.yml"));
                }
                if (userFile.exists() && !userFile.delete()) {
                    plugin.getLogger().warning("Could not delete EssentialsX user file for: " + playerId);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to backup/wipe EssentialsX data for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> handleUnwipe(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            File userFile = getEssentialsUserFile(playerId);
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            File backupFile = new File(backupDir, "essentials.yml");

            if (!backupFile.exists()) {
                return;
            }

            try {
                copyFile(backupFile, userFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to restore EssentialsX data for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
