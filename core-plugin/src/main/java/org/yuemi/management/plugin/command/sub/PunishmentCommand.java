package org.yuemi.management.plugin.command.sub;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.plugin.ManagementPanelPlugin;
import org.yuemi.management.plugin.command.helper.CommandHelper;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class PunishmentCommand implements SubCommand {

    protected final ManagementPanelPlugin plugin;
    private final boolean hasDuration;

    public PunishmentCommand(ManagementPanelPlugin plugin, boolean hasDuration) {
        this.plugin = plugin;
        this.hasDuration = hasDuration;
    }

    protected abstract CompletableFuture<PunishmentResult> executePunishment(
            UUID targetId, @Nullable String reason, @Nullable Duration duration, String sourceName
    );

    protected void onPunishmentSuccess(CommandSender sender, String targetName, UUID targetId, Set<String> flags) {
        // Can be overridden (e.g. for post-punishment logic)
    }

    protected boolean shouldWipeBefore(Set<String> flags) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        String sourceName = sender.getName();

        Set<String> flags = new HashSet<>();
        List<String> cleanArgsList = new ArrayList<>();
        cleanArgsList.add(args[0]); // subcommand
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                flags.add(args[i].toLowerCase());
            } else {
                cleanArgsList.add(args[i]);
            }
        }
        String[] cleanArgs = cleanArgsList.toArray(new String[0]);

        if (cleanArgs.length < 2) {
            CommandHelper.sendMsg(sender, "Target player not specified.", NamedTextColor.RED);
            return;
        }

        String targetName = cleanArgs[1];

        CompletableFuture.runAsync(() -> {
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            UUID uuid = op.getUniqueId();

            if (getName().equalsIgnoreCase("ban")) {
                if (plugin.getPunishmentServiceImpl().isBanned(uuid)) {
                    CommandHelper.sendMsg(sender, targetName + " is already banned.", NamedTextColor.RED);
                    return;
                }
            } else if (getName().equalsIgnoreCase("unban")) {
                if (!plugin.getPunishmentServiceImpl().isBanned(uuid)) {
                    CommandHelper.sendMsg(sender, targetName + " is not banned.", NamedTextColor.RED);
                    return;
                }
            } else if (getName().equalsIgnoreCase("mute")) {
                if (plugin.getPunishmentServiceImpl().isMuted(uuid)) {
                    CommandHelper.sendMsg(sender, targetName + " is already muted.", NamedTextColor.RED);
                    return;
                }
            } else if (getName().equalsIgnoreCase("unmute")) {
                if (!plugin.getPunishmentServiceImpl().isMuted(uuid)) {
                    CommandHelper.sendMsg(sender, targetName + " is not muted.", NamedTextColor.RED);
                    return;
                }
            }

            Duration duration = null;
            String reason = null;

            if (hasDuration) {
                if (cleanArgs.length >= 3) {
                    duration = CommandHelper.parseDuration(cleanArgs[2]);
                    if (duration != null) {
                        if (cleanArgs.length >= 4) {
                            reason = CommandHelper.joinArgs(cleanArgs, 3);
                        }
                    } else {
                        reason = CommandHelper.joinArgs(cleanArgs, 2);
                    }
                }
            } else {
                if (cleanArgs.length >= 3) {
                    reason = CommandHelper.joinArgs(cleanArgs, 2);
                }
            }

            // Fallback for warn
            if (getName().equals("warn") && (reason == null || reason.isBlank())) {
                reason = "No reason provided";
            }

            final String finalReason = reason;
            final Duration finalDuration = duration;

            if (shouldWipeBefore(flags)) {
                CommandHelper.sendMsg(sender, "Auto-wiping " + targetName + "...", NamedTextColor.YELLOW);
                plugin.getWipeServiceImpl().wipe(uuid)
                        .thenAccept(backupId -> {
                            CommandHelper.sendMsg(sender, "Successfully wiped " + targetName + ". Backup created: " + backupId, NamedTextColor.GREEN);
                            doPunishment(uuid, finalReason, finalDuration, sourceName, sender, targetName, flags);
                        })
                        .exceptionally(t -> {
                            CommandHelper.sendMsg(sender, "Failed to auto-wipe player " + targetName + ": " + t.getCause().getMessage(), NamedTextColor.RED);
                            doPunishment(uuid, finalReason, finalDuration, sourceName, sender, targetName, flags);
                            return null;
                        });
            } else {
                doPunishment(uuid, finalReason, finalDuration, sourceName, sender, targetName, flags);
            }
        });
    }

    private void doPunishment(UUID targetId, String reason, Duration duration, String sourceName, CommandSender sender, String targetName, Set<String> flags) {
        executePunishment(targetId, reason, duration, sourceName)
                .thenAccept(result -> {
                    if (result.success()) {
                        CommandHelper.sendMsg(sender, result.message(), NamedTextColor.GREEN);
                        onPunishmentSuccess(sender, targetName, targetId, flags);
                    } else {
                        CommandHelper.sendMsg(sender, "Failed: " + result.message(), NamedTextColor.RED);
                    }
                })
                .exceptionally(t -> {
                    CommandHelper.sendMsg(sender, "Error processing action: " + t.getMessage(), NamedTextColor.RED);
                    return null;
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

        if (hasDuration && args.length == 3 && !args[2].startsWith("--")) {
            return List.of("30m", "1h", "12h", "1d", "7d", "30d").stream()
                    .filter(t -> t.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length >= 3) {
            List<String> suggestions = new ArrayList<>();
            addExtraTabCompletions(suggestions, args);
            if (!suggestions.isEmpty()) {
                String lastArg = args[args.length - 1].toLowerCase();
                return suggestions.stream()
                        .filter(t -> t.startsWith(lastArg))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    protected void addExtraTabCompletions(List<String> suggestions, String[] args) {
        // To be overridden (e.g. for ban's --wipe)
    }
}
