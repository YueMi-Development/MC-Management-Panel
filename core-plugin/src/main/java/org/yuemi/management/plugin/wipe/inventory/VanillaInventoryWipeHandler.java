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
    public void preWipeSync(@NotNull UUID playerId) {
        org.bukkit.entity.Player p = Bukkit.getPlayer(playerId);
        if (p != null) {
            p.getInventory().clear();
            p.setExp(0);
            p.setLevel(0);
            p.saveData();
            plugin.getLogger().info("Synchronously cleared live inventory for " + p.getName());
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> createBackup(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            File worldFolder = getDefaultWorldFolder();
            File playerdataFile = new File(new File(worldFolder, "playerdata"), playerId.toString() + ".dat");
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);

            if (backupDir.exists()) {
                try {
                    copyFile(playerdataFile, new File(backupDir, "playerdata.dat"));
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to backup vanilla inventory for " + playerId + ": " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> executeWipe(@NotNull UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            File worldFolder = getDefaultWorldFolder();
            File playerdataFile = new File(new File(worldFolder, "playerdata"), playerId.toString() + ".dat");

            if (playerdataFile.exists()) {
                if (playerdataFile.delete()) {
                    plugin.getLogger().info("Successfully deleted playerdata.dat for " + playerId);
                } else {
                    plugin.getLogger().warning("Could not delete playerdata.dat file for: " + playerId + ". It may be locked or in use.");
                }
            } else {
                plugin.getLogger().info("No playerdata.dat file found for " + playerId + " to delete.");
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> executeRestore(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.runAsync(() -> {
            File backupDir = plugin.getWipeServiceImpl().getBackupDirectory(playerId, backupId);
            File backupFile = new File(backupDir, "playerdata.dat");

            if (!backupFile.exists()) {
                return;
            }

            File worldFolder = getDefaultWorldFolder();
            File playerdataFile = new File(new File(worldFolder, "playerdata"), playerId.toString() + ".dat");

            try {
                copyFile(backupFile, playerdataFile);
                plugin.getLogger().info("Restored playerdata for " + playerId);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to restore vanilla inventory for " + playerId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
