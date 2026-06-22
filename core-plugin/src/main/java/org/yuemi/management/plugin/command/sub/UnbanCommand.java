package org.yuemi.management.plugin.command.sub;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.api.punishment.PunishmentResult;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnbanCommand extends PunishmentCommand {

    public UnbanCommand(ManagementPanelPlugin plugin) {
        super(plugin, false);
    }

    @Override
    public @NotNull String getName() {
        return "unban";
    }

    @Override
    public @NotNull String getPermission() {
        return "unban";
    }

    @Override
    protected CompletableFuture<PunishmentResult> executePunishment(UUID targetId, @Nullable String reason, @Nullable Duration duration, String sourceName) {
        return plugin.getPunishmentServiceImpl().unban(targetId, reason, sourceName);
    }
}
