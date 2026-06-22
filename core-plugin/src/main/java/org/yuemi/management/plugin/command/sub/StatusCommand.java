package org.yuemi.management.plugin.command.sub;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.plugin.ManagementPanelPlugin;
import org.yuemi.management.plugin.command.helper.CommandHelper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StatusCommand implements SubCommand {

    private final ManagementPanelPlugin plugin;

    public StatusCommand(ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "status";
    }

    @Override
    public @NotNull String getPermission() {
        return "status";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandHelper.sendMsg(sender, "Active Punishment Handler: " + plugin.getPunishmentServiceImpl().getActiveHandler().getName(), NamedTextColor.GREEN);
        String handlers = plugin.getWipeServiceImpl().getHandlers().stream().map(h -> h.getName()).collect(Collectors.joining(", "));
        CommandHelper.sendMsg(sender, "Registered Wipe Handlers: " + handlers, NamedTextColor.GREEN);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
