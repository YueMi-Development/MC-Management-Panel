package org.yuemi.management.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public final class ConfigMigrator {

    private final ManagementPanelPlugin plugin;

    public ConfigMigrator(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean requiresMigration(int targetVersion) {
        int currentVersion = plugin.getConfig().getInt("config-version", 0);
        return currentVersion < targetVersion;
    }

    public void migrate(int targetVersion) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        int oldVersion = plugin.getConfig().getInt("config-version", 0);

        plugin.getLogger().info("Found outdated config.yml (v" + oldVersion + "). Migrating to v" + targetVersion + "...");

        // 1. Backup the existing config
        File backupFile = new File(plugin.getDataFolder(), "config-backup-v" + oldVersion + ".yml");
        try {
            if (configFile.exists()) {
                Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Created backup of old configuration at: " + backupFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to backup config.yml! Migration aborted to prevent data loss.", e);
            return;
        }

        // Load the old config into memory
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(backupFile);

        // 2. Overwrite config.yml with the fresh default from the jar (contains new comments and defaults)
        plugin.saveResource("config.yml", true);
        
        // Reload to load the fresh config into plugin.getConfig()
        plugin.reloadConfig();
        FileConfiguration newConfig = plugin.getConfig();

        // 3. Copy user values from the old config to the new config
        // We only copy keys that exist in the old config, but we also ensure we aren't copying the old version number
        for (String key : oldConfig.getKeys(true)) {
            if (key.equals("config-version")) {
                continue;
            }
            
            // If the old config has a value, and it's not a configuration section itself, we copy it over.
            // This naive approach copies everything. For complex migrations (renamed keys), we would add logic here.
            if (!oldConfig.isConfigurationSection(key)) {
                newConfig.set(key, oldConfig.get(key));
            }
        }

        // Apply version-specific migrations here
        if (oldVersion == 1) {
             plugin.getLogger().info("Migrating from v1 to v2: Added 'wipe.auto-wipe-on-ban' setting.");
             if (!oldConfig.contains("wipe.auto-wipe-on-ban")) {
                 newConfig.set("wipe.auto-wipe-on-ban", false);
             }
        }

        // 4. Save the updated config with the user's migrated values
        newConfig.set("config-version", targetVersion);
        plugin.saveConfig();

        plugin.getLogger().info("Successfully migrated config.yml to v" + targetVersion + "!");
    }
}
