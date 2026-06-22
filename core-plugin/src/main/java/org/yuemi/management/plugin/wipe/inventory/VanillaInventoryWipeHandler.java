package org.yuemi.management.plugin.wipe.inventory;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.yuemi.management.api.wipe.WipeHandler;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class VanillaInventoryWipeHandler implements WipeHandler {

    private final ManagementPanelPlugin plugin;

    public VanillaInventoryWipeHandler(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "vanilla";
    }

    private File getDefaultWorldFolder() {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            return new File(".");
        }
        return worlds.get(0).getWorldFolder();
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
            File worldFolder = getDefaultWorldFolder();
            File playerdataFile = new File(new File(worldFolder, "playerdata"), playerId.toString() + ".dat");
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);

            try {
                // Back up file if backup directory exists
                if (backupDir.exists()) {
                    copyFile(playerdataFile, new File(backupDir, "playerdata.dat"));
                }

                // Delete file (Note: this deletes entire playerdata including location/health)
                if (playerdataFile.exists() && !playerdataFile.delete()) {
                    plugin.getLogger().warning("Could not delete playerdata file for: " + playerId);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to backup/wipe vanilla inventory for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> handleUnwipe(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            File worldFolder = getDefaultWorldFolder();
            File playerdataFile = new File(new File(worldFolder, "playerdata"), playerId.toString() + ".dat");
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            
            if (!backupDir.exists()) {
                return;
            }

            try {
                // Restore file
                copyFile(new File(backupDir, "playerdata.dat"), playerdataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to restore vanilla inventory for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
