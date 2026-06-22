package org.yuemi.management.plugin.command.sub;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MuteCommand extends PunishmentCommand {

    public MuteCommand(ManagementPanelPlugin plugin) {
        super(plugin, true);
    }

    @Override
    public @NotNull String getName() {
        return "mute";
    }

    @Override
    public @NotNull String getPermission() {
        return "mute";
    }

    @Override
    protected CompletableFuture<PunishmentResult> executePunishment(UUID targetId, @Nullable String reason, @Nullable Duration duration, String sourceName) {
        return plugin.getPunishmentServiceImpl().mute(targetId, reason, duration, sourceName);
    }
}
