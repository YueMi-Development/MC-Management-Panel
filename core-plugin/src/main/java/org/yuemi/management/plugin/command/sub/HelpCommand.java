package org.yuemi.management.plugin.command.sub;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.util.Collections;
import java.util.List;

public class HelpCommand implements SubCommand {

    private final ManagementPanelPlugin plugin;

    public HelpCommand(ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "help";
    }

    @Override
    public @NotNull String getPermission() {
        return "help"; // managementpanel.command.help
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage(Component.text("=== Management Panel Help ===", NamedTextColor.DARK_GREEN));
        sender.sendMessage(Component.text("/mp wipe <player> - Full player data wipe (creates backup)", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp unwipe <player> [backupId] - Restore player data from backup", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp ban <player> [duration] [reason] [--wipe|--nowipe] - Ban player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp unban <player> [reason] - Unban player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp mute <player> [duration] [reason] - Mute player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp unmute <player> [reason] - Unmute player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp kick <player> [reason] - Kick player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp warn <player> <reason> - Warn player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp status - View active integrations and handlers", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp reload - Reload plugin configurations", NamedTextColor.YELLOW));
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
