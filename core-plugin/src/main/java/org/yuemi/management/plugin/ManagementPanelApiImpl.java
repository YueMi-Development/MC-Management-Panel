package org.yuemi.management.plugin;

import org.jetbrains.annotations.NotNull;
import org.yuemi.management.api.ManagementPanelApi;
import org.yuemi.management.api.punishment.PunishmentService;
import org.yuemi.management.api.wipe.WipeService;

public final class ManagementPanelApiImpl implements ManagementPanelApi {

    private final PunishmentService punishmentService;
    private final WipeService wipeService;

    public ManagementPanelApiImpl(
            @NotNull PunishmentService punishmentService,
            @NotNull WipeService wipeService
    ) {
        this.punishmentService = punishmentService;
        this.wipeService = wipeService;
    }

    @Override
    public @NotNull PunishmentService getPunishmentService() {
        return punishmentService;
    }

    @Override
    public @NotNull WipeService getWipeService() {
        return wipeService;
    }
}
