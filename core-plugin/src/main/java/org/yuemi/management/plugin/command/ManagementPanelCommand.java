package org.yuemi.management.plugin.command;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.plugin.ManagementPanelPlugin;
import org.yuemi.management.plugin.command.helper.CommandHelper;
import org.yuemi.management.plugin.command.sub.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ManagementPanelCommand implements CommandExecutor, TabCompleter {

    private final ManagementPanelPlugin plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ManagementPanelCommand(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }

    private void registerSubCommands() {
        subCommands.put("help", new HelpCommand(plugin));
        subCommands.put("reload", new ReloadCommand(plugin));
        subCommands.put("status", new StatusCommand(plugin));
        subCommands.put("wipe", new WipeCommand(plugin));
        subCommands.put("unwipe", new UnwipeCommand(plugin));
        subCommands.put("ban", new BanCommand(plugin));
        subCommands.put("unban", new UnbanCommand(plugin));
        subCommands.put("mute", new MuteCommand(plugin));
        subCommands.put("unmute", new UnmuteCommand(plugin));
        subCommands.put("kick", new KickCommand(plugin));
        subCommands.put("warn", new WarnCommand(plugin));
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            CommandHelper.sendMsg(sender, "Use /mp help to see commands.", NamedTextColor.YELLOW);
            return true;
        }

        String subName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subName);

        if (subCommand == null) {
            CommandHelper.sendMsg(sender, "Unknown subcommand. Use /mp help.", NamedTextColor.RED);
            return true;
        }

        if (!CommandHelper.hasPermission(sender, subCommand.getPermission())) {
            CommandHelper.sendMsg(sender, "You do not have permission to run this command.", NamedTextColor.RED);
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .filter(name -> {
                        SubCommand sub = subCommands.get(name);
                        return sub != null && CommandHelper.hasPermission(sender, sub.getPermission());
                    })
                    .toList();
        }

        String subName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subName);

        if (subCommand != null && CommandHelper.hasPermission(sender, subCommand.getPermission())) {
            return subCommand.tabComplete(sender, args);
        }

        return Collections.emptyList();
    }
}
