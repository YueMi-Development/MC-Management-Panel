package org.yuemi.management.plugin.command.helper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public final class CommandHelper {

    private CommandHelper() {}

    public static boolean hasPermission(@NotNull CommandSender sender, @NotNull String subPerm) {
        return sender.hasPermission("managementpanel.admin") || sender.hasPermission("managementpanel.command." + subPerm);
    }

    public static void sendMsg(@NotNull CommandSender sender, @NotNull String message, @NotNull NamedTextColor color) {
        sender.sendMessage(Component.text("[ManagementPanel] " + message, color));
    }

    public static @Nullable Duration parseDuration(@Nullable String input) {
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

    public static @NotNull String joinArgs(@NotNull String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        return sb.toString().trim();
    }
}
