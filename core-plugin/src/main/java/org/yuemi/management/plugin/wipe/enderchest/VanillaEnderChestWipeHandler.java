package org.yuemi.management.plugin.wipe.enderchest;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.yuemi.management.api.wipe.WipeHandler;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class VanillaEnderChestWipeHandler implements WipeHandler {

    private final ManagementPanelPlugin plugin;

    public VanillaEnderChestWipeHandler(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "vanilla-enderchest";
    }

    @Override
    public void preWipeSync(@NotNull UUID playerId) {
        org.bukkit.entity.Player p = Bukkit.getPlayer(playerId);
        if (p != null) {
            p.getEnderChest().clear();
            p.saveData();
            plugin.getLogger().info("Synchronously cleared live ender chest for " + p.getName());
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
                    copyFile(playerdataFile, new File(backupDir, "playerdata_enderchest.dat"));
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to backup vanilla ender chest for " + playerId + ": " + e.getMessage());
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
                    plugin.getLogger().info("Successfully deleted playerdata.dat for " + playerId + " (EnderChest Wipe)");
                } else {
                    plugin.getLogger().warning("Could not delete playerdata.dat file for: " + playerId + " (EnderChest Wipe)");
                }
            }
        });
    }

    @Override
    public boolean supportsRestore() {
        return false;
    }

    @Override
    public @NotNull CompletableFuture<Void> executeRestore(@NotNull UUID playerId, @NotNull String backupId) {
        return CompletableFuture.completedFuture(null);
    }

    private File getDefaultWorldFolder() {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            return new File(".");
        }
        return worlds.get(0).getWorldFolder();
    }

    private void copyFile(File source, File dest) throws IOException {
        if (!source.exists()) {
            return;
        }
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
            throw new IOException("Failed to create parent directories for: " + dest.getPath());
        }
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
