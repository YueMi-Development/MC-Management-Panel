package org.yuemi.management.api;

import org.jetbrains.annotations.NotNull;
import org.yuemi.management.api.punishment.PunishmentService;
import org.yuemi.management.api.wipe.WipeService;

/**
 * The main API interface for the Management Panel.
 */
public interface ManagementPanelApi {

    /**
     * Gets the active punishment service.
     *
     * @return the punishment service
     */
    @NotNull PunishmentService getPunishmentService();

    /**
     * Gets the active wipe service.
     *
     * @return the wipe service
     */
    @NotNull WipeService getWipeService();
}
