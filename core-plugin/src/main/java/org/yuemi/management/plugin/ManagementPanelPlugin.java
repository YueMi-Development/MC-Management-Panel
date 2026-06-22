package org.yuemi.management.plugin;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.yuemi.management.api.ManagementPanelApi;
import org.yuemi.management.api.ManagementPanelProvider;
import org.yuemi.management.plugin.command.ManagementPanelCommand;
import org.yuemi.management.plugin.punishment.PunishmentServiceImpl;
import org.yuemi.management.plugin.wipe.WipeServiceImpl;
import org.yuemi.management.plugin.config.ConfigManager;

import java.util.logging.Level;

public final class ManagementPanelPlugin extends JavaPlugin {

    private ManagementPanelApi api;
    private PunishmentServiceImpl punishmentService;
    private WipeServiceImpl wipeService;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Initialize Configuration and run migrations if necessary
        this.configManager = new ConfigManager(this);

        // Initialize Services
        this.punishmentService = new PunishmentServiceImpl(this);
        this.wipeService = new WipeServiceImpl(this);
        this.api = new ManagementPanelApiImpl(punishmentService, wipeService);

        // Register API in Provider
        ManagementPanelProvider.register(api);

        // Register API in Bukkit ServicesManager
        getServer().getServicesManager().register(
                ManagementPanelApi.class,
                api,
                this,
                ServicePriority.Normal
        );

        // Register Commands
        ManagementPanelCommand cmd = new ManagementPanelCommand(this);
        var command = getCommand("managementpanel");
        if (command != null) {
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        }

        getLogger().info("Management Panel has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        // Unregister API in Bukkit ServicesManager
        getServer().getServicesManager().unregister(ManagementPanelApi.class, api);

        // Unregister API in Provider
        ManagementPanelProvider.unregister();

        getLogger().info("Management Panel has been disabled.");
    }

    public PunishmentServiceImpl getPunishmentServiceImpl() {
        return punishmentService;
    }

    public WipeServiceImpl getWipeServiceImpl() {
        return wipeService;
    }
}
