package org.yuemi.management.plugin.command.sub;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.plugin.ManagementPanelPlugin;
import org.yuemi.management.plugin.command.helper.CommandHelper;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements SubCommand {

    private final ManagementPanelPlugin plugin;

    public ReloadCommand(ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull String getPermission() {
        return "reload";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        plugin.reloadConfig();
        plugin.getPunishmentServiceImpl().reload();
        plugin.getWipeServiceImpl().reload();
        CommandHelper.sendMsg(sender, "Configuration and integrations reloaded.", NamedTextColor.GREEN);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
