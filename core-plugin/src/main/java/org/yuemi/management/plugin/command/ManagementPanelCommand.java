package org.yuemi.management.plugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ManagementPanelCommand implements CommandExecutor, TabCompleter {

    private final ManagementPanelPlugin plugin;

    public ManagementPanelCommand(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean hasPermission(CommandSender sender, String subPerm) {
        return sender.hasPermission("managementpanel.admin") || sender.hasPermission("managementpanel.command." + subPerm);
    }

    private void sendMsg(CommandSender sender, String message, NamedTextColor color) {
        sender.sendMessage(Component.text("[ManagementPanel] " + message, color));
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            sendMsg(sender, "Use /mp help to see commands.", NamedTextColor.YELLOW);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help":
                sendHelp(sender);
                return true;
            case "reload":
                if (!hasPermission(sender, "reload")) {
                    sendMsg(sender, "You do not have permission to run this command.", NamedTextColor.RED);
                    return true;
                }
                plugin.reloadConfig();
                plugin.getPunishmentServiceImpl().reload();
                plugin.getWipeServiceImpl().reload();
                sendMsg(sender, "Configuration and integrations reloaded.", NamedTextColor.GREEN);
                return true;
            case "status":
                if (!hasPermission(sender, "status")) {
                    sendMsg(sender, "You do not have permission to run this command.", NamedTextColor.RED);
                    return true;
                }
                sendMsg(sender, "Active Punishment Handler: " + plugin.getPunishmentServiceImpl().getActiveHandler().getName(), NamedTextColor.GREEN);
                sendMsg(sender, "Registered Wipe Handlers: " + plugin.getWipeServiceImpl().getHandlers().stream().map(h -> h.getName()).collect(Collectors.joining(", ")), NamedTextColor.GREEN);
                return true;
            case "wipe":
                if (!hasPermission(sender, "wipe")) {
                    sendMsg(sender, "You do not have permission to run this command.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 2) {
                    sendMsg(sender, "Usage: /mp wipe <player>", NamedTextColor.RED);
                    return true;
                }
                executeWipe(sender, args[1]);
                return true;
            case "unwipe":
                if (!hasPermission(sender, "unwipe")) {
                    sendMsg(sender, "You do not have permission to run this command.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 2) {
                    sendMsg(sender, "Usage: /mp unwipe <player> [backupId]", NamedTextColor.RED);
                    return true;
                }
                String backupId = args.length >= 3 ? args[2] : null;
                executeUnwipe(sender, args[1], backupId);
                return true;
            case "ban":
                if (!hasPermission(sender, "ban")) {
                    sendMsg(sender, "You do not have permission.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 2) {
                    sendMsg(sender, "Usage: /mp ban <player> [duration] [reason]", NamedTextColor.RED);
                    return true;
                }
                handlePunishment(sender, args, "ban");
                return true;
            case "unban":
                if (!hasPermission(sender, "unban")) {
                    sendMsg(sender, "You do not have permission.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 2) {
                    sendMsg(sender, "Usage: /mp unban <player> [reason]", NamedTextColor.RED);
                    return true;
                }
                handlePunishment(sender, args, "unban");
                return true;
            case "mute":
                if (!hasPermission(sender, "mute")) {
                    sendMsg(sender, "You do not have permission.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 2) {
                    sendMsg(sender, "Usage: /mp mute <player> [duration] [reason]", NamedTextColor.RED);
                    return true;
                }
                handlePunishment(sender, args, "mute");
                return true;
            case "unmute":
                if (!hasPermission(sender, "unmute")) {
                    sendMsg(sender, "You do not have permission.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 2) {
                    sendMsg(sender, "Usage: /mp unmute <player> [reason]", NamedTextColor.RED);
                    return true;
                }
                handlePunishment(sender, args, "unmute");
                return true;
            case "kick":
                if (!hasPermission(sender, "kick")) {
                    sendMsg(sender, "You do not have permission.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 2) {
                    sendMsg(sender, "Usage: /mp kick <player> [reason]", NamedTextColor.RED);
                    return true;
                }
                handlePunishment(sender, args, "kick");
                return true;
            case "warn":
                if (!hasPermission(sender, "warn")) {
                    sendMsg(sender, "You do not have permission.", NamedTextColor.RED);
                    return true;
                }
                if (args.length < 3) {
                    sendMsg(sender, "Usage: /mp warn <player> <reason>", NamedTextColor.RED);
                    return true;
                }
                handlePunishment(sender, args, "warn");
                return true;
            default:
                sendMsg(sender, "Unknown subcommand. Use /mp help.", NamedTextColor.RED);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Management Panel Help ===", NamedTextColor.DARK_GREEN));
        sender.sendMessage(Component.text("/mp wipe <player> - Full player data wipe (creates backup)", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp unwipe <player> [backupId] - Restore player data from backup", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp ban <player> [duration] [reason] - Ban player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp unban <player> [reason] - Unban player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp mute <player> [duration] [reason] - Mute player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp unmute <player> [reason] - Unmute player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp kick <player> [reason] - Kick player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp warn <player> <reason> - Warn player", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp status - View active integrations and handlers", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/mp reload - Reload plugin configurations", NamedTextColor.YELLOW));
    }

    @SuppressWarnings("deprecation")
    private void executeWipe(CommandSender sender, String targetName) {
        sendMsg(sender, "Resolving target and starting wipe process for: " + targetName, NamedTextColor.YELLOW);
        CompletableFuture.runAsync(() -> {
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            plugin.getWipeServiceImpl().wipe(op.getUniqueId())
                    .thenAccept(backupId -> sendMsg(sender, "Successfully wiped " + targetName + ". Backup created: " + backupId, NamedTextColor.GREEN))
                    .exceptionally(t -> {
                        sendMsg(sender, "Failed to wipe player " + targetName + ": " + t.getCause().getMessage(), NamedTextColor.RED);
                        return null;
                    });
        });
    }

    @SuppressWarnings("deprecation")
    private void executeUnwipe(CommandSender sender, String targetName, @Nullable String backupId) {
        sendMsg(sender, "Resolving target and starting restore process for: " + targetName, NamedTextColor.YELLOW);
        CompletableFuture.runAsync(() -> {
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            plugin.getWipeServiceImpl().unwipe(op.getUniqueId(), backupId)
                    .thenRun(() -> sendMsg(sender, "Successfully restored " + targetName + " from backup.", NamedTextColor.GREEN))
                    .exceptionally(t -> {
                        sendMsg(sender, "Failed to restore player " + targetName + ": " + t.getCause().getMessage(), NamedTextColor.RED);
                        return null;
                    });
        });
    }

    @SuppressWarnings("deprecation")
    private void handlePunishment(CommandSender sender, String[] args, String type) {
        String targetName = args[1];
        String sourceName = sender.getName();

        CompletableFuture.runAsync(() -> {
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            UUID uuid = op.getUniqueId();

            Duration duration = null;
            String reason = null;

            if (type.equals("ban") || type.equals("mute")) {
                if (args.length >= 3) {
                    duration = parseDuration(args[2]);
                    if (duration != null) {
                        // args[2] was a duration, reason starts at args[3]
                        if (args.length >= 4) {
                            reason = joinArgs(args, 3);
                        }
                    } else {
                        // args[2] was not a duration, assume it is part of reason
                        reason = joinArgs(args, 2);
                    }
                }
            } else if (type.equals("unban") || type.equals("unmute") || type.equals("kick")) {
                if (args.length >= 3) {
                    reason = joinArgs(args, 2);
                }
            } else if (type.equals("warn")) {
                reason = joinArgs(args, 2);
            }

            CompletableFuture<PunishmentResult> future;
            var service = plugin.getPunishmentServiceImpl();

            switch (type) {
                case "ban" -> future = service.ban(uuid, reason, duration, sourceName);
                case "unban" -> future = service.unban(uuid, reason, sourceName);
                case "mute" -> future = service.mute(uuid, reason, duration, sourceName);
                case "unmute" -> future = service.unmute(uuid, reason, sourceName);
                case "kick" -> future = service.kick(uuid, reason, sourceName);
                case "warn" -> future = service.warn(uuid, reason != null ? reason : "No reason provided", sourceName);
                default -> {
                    sendMsg(sender, "Invalid punishment type.", NamedTextColor.RED);
                    return;
                }
            }

            future.thenAccept(result -> {
                if (result.success()) {
                    sendMsg(sender, result.message(), NamedTextColor.GREEN);
                } else {
                    sendMsg(sender, "Failed: " + result.message(), NamedTextColor.RED);
                }
            }).exceptionally(t -> {
                sendMsg(sender, "Error processing action: " + t.getMessage(), NamedTextColor.RED);
                return null;
            });
        });
    }

    private @Nullable Duration parseDuration(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            long value = Long.parseLong(input.substring(0, input.length() - 1));
            char unit = input.toLowerCase().charAt(input.length() - 1);
            return switch (unit) {
                case 'd' -> Duration.ofDays(value);
                case 'h' -> Duration.ofHours(value);
                case 'm' -> Duration.ofMinutes(value);
                case 's' -> Duration.ofSeconds(value);
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private String joinArgs(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("help", "wipe", "unwipe", "ban", "unban", "mute", "unmute", "kick", "warn", "status", "reload"));
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (List.of("wipe", "unwipe", "ban", "unban", "mute", "unmute", "kick", "warn").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("ban") || sub.equals("mute")) {
                List<String> times = List.of("30m", "1h", "12h", "1d", "7d", "30d");
                return times.stream()
                        .filter(t -> t.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
