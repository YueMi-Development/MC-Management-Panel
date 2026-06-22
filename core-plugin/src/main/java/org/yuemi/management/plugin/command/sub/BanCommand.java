package org.yuemi.management.plugin.command.sub;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.plugin.ManagementPanelPlugin;
import org.yuemi.management.plugin.command.helper.CommandHelper;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanCommand extends PunishmentCommand {

    public BanCommand(ManagementPanelPlugin plugin) {
        super(plugin, true);
    }

    @Override
    public @NotNull String getName() {
        return "ban";
    }

    @Override
    public @NotNull String getPermission() {
        return "ban";
    }

    @Override
    protected CompletableFuture<PunishmentResult> executePunishment(UUID targetId, @Nullable String reason, @Nullable Duration duration, String sourceName) {
        return plugin.getPunishmentServiceImpl().ban(targetId, reason, duration, sourceName);
    }

    @Override
    protected void onPunishmentSuccess(CommandSender sender, String targetName, UUID targetId, Set<String> flags) {
        boolean autoWipe = plugin.getConfig().getBoolean("wipe.auto-wipe-on-ban", false);
        boolean shouldWipe = false;
        
        if (flags.contains("--wipe")) {
            shouldWipe = true;
        } else if (!flags.contains("--nowipe") && autoWipe) {
            shouldWipe = true;
        }

        if (shouldWipe) {
            CommandHelper.sendMsg(sender, "Auto-wiping " + targetName + "...", NamedTextColor.YELLOW);
            plugin.getWipeServiceImpl().wipe(targetId)
                    .thenAccept(backupId -> CommandHelper.sendMsg(sender, "Successfully wiped " + targetName + ". Backup created: " + backupId, NamedTextColor.GREEN))
                    .exceptionally(t -> {
                        CommandHelper.sendMsg(sender, "Failed to auto-wipe player " + targetName + ": " + t.getCause().getMessage(), NamedTextColor.RED);
                        return null;
                    });
        }
    }

    @Override
    protected void addExtraTabCompletions(List<String> suggestions, String[] args) {
        suggestions.add("--wipe");
        suggestions.add("--nowipe");
    }
}
