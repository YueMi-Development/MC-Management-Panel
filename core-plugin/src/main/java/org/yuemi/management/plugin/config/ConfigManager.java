package org.yuemi.management.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yuemi.management.plugin.ManagementPanelPlugin;

public final class ConfigManager {

    private final ManagementPanelPlugin plugin;
    public static final int CURRENT_VERSION = 4;

    public ConfigManager(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        // Save the default config if it doesn't exist
        plugin.saveDefaultConfig();

        // Reload to ensure we have the latest from disk
        plugin.reloadConfig();

        // Check if migration is needed
        ConfigMigrator migrator = new ConfigMigrator(plugin);
        if (migrator.requiresMigration(CURRENT_VERSION)) {
            migrator.migrate(CURRENT_VERSION);
            // Reload again after migration to load the newly migrated values
            plugin.reloadConfig();
        }
    }

    public void reload() {
        plugin.reloadConfig();
        // We could theoretically check for migrations again here, but usually, 
        // migrations only happen on startup/update.
    }

    @NotNull
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
