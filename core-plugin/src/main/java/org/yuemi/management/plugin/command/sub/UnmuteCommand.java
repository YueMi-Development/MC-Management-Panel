package org.yuemi.management.plugin.command.sub;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnmuteCommand extends PunishmentCommand {

    public UnmuteCommand(ManagementPanelPlugin plugin) {
        super(plugin, false);
    }

    @Override
    public @NotNull String getName() {
        return "unmute";
    }

    @Override
    public @NotNull String getPermission() {
        return "unmute";
    }

    @Override
    protected CompletableFuture<PunishmentResult> executePunishment(UUID targetId, @Nullable String reason, @Nullable Duration duration, String sourceName) {
        return plugin.getPunishmentServiceImpl().unmute(targetId, reason, sourceName);
    }
}
