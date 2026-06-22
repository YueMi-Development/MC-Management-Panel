package org.yuemi.management.plugin.command.sub;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.plugin.ManagementPanelPlugin;
import org.yuemi.management.plugin.command.helper.CommandHelper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UnwipeCommand implements SubCommand {

    private final ManagementPanelPlugin plugin;

    public UnwipeCommand(ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "unwipe";
    }

    @Override
    public @NotNull String getPermission() {
        return "unwipe";
    }

    @Override
    @SuppressWarnings("deprecation")
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            CommandHelper.sendMsg(sender, "Usage: /mp unwipe <player> [backupId]", NamedTextColor.RED);
            return;
        }

        String targetName = args[1];
        String backupId = args.length >= 3 ? args[2] : null;

        CommandHelper.sendMsg(sender, "Resolving target and starting restore process for: " + targetName, NamedTextColor.YELLOW);
        
        CompletableFuture.runAsync(() -> {
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            plugin.getWipeServiceImpl().unwipe(op.getUniqueId(), backupId)
                    .thenRun(() -> CommandHelper.sendMsg(sender, "Successfully restored " + targetName + " from backup.", NamedTextColor.GREEN))
                    .exceptionally(t -> {
                        CommandHelper.sendMsg(sender, "Failed to restore player " + targetName + ": " + t.getCause().getMessage(), NamedTextColor.RED);
                        return null;
                    });
        });
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
