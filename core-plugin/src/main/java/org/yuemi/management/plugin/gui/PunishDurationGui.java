package org.yuemi.management.plugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yuemi.libs.api.gui.Gui;
import org.yuemi.libs.api.gui.GuiItem;
import org.yuemi.libs.api.gui.GuiLayerBuilder;
import org.yuemi.libs.api.gui.GuiProvider;
import org.yuemi.management.plugin.ManagementPanelPlugin;

public final class PunishDurationGui extends BasePanelGui {

    public PunishDurationGui(@NotNull ManagementPanelPlugin plugin) {
        super(plugin);
    }

    public void open(@NotNull Player viewer, @NotNull OfflinePlayer target, @NotNull String type) {
        var api = GuiProvider.getApi();
        if (api == null) return;

        Gui gui = api.createBuilder()
                .title("Duration for " + type + " - " + target.getName())
                .rows(3)
                .createLayer("background", 0, layer -> layer.fill(createFiller()))
                .createLayer("durations", 1, layer -> {
                    addDurationItem(layer, 10, viewer, target, type, "15m", "15 Minutes");
                    addDurationItem(layer, 11, viewer, target, type, "1h", "1 Hour");
                    addDurationItem(layer, 12, viewer, target, type, "12h", "12 Hours");
                    addDurationItem(layer, 13, viewer, target, type, "1d", "1 Day");
                    addDurationItem(layer, 14, viewer, target, type, "7d", "7 Days");
                    addDurationItem(layer, 15, viewer, target, type, "30d", "30 Days");

                    ItemStack permItem = new ItemStack(Material.REDSTONE_BLOCK);
                    setDisplayNameAndLore(permItem, Component.text("Permanent", NamedTextColor.RED), null);
                    layer.setItem(16, GuiItem.builder()
                            .item(permItem)
                            .onClick((p, ctx) -> {
                                p.closeInventory();
                                p.performCommand("mp " + type + " " + target.getName() + " Permanent punishment via GUI");
                            })
                            .build());

                    ItemStack backArrow = new ItemStack(Material.ARROW);
                    setDisplayNameAndLore(backArrow, Component.text("Back to Actions", NamedTextColor.GRAY), null);
                    layer.setItem(22, GuiItem.builder()
                            .item(backArrow)
                            .onClick((p, ctx) -> new PlayerActionGui(plugin).open(p, target))
                            .build());
                })
                .build();

        gui.open(viewer);
    }

    private void addDurationItem(
            @NotNull GuiLayerBuilder layer,
            int slot,
            @NotNull Player viewer,
            @NotNull OfflinePlayer target,
            @NotNull String type,
            @NotNull String durationString,
            @NotNull String label
    ) {
        ItemStack item = new ItemStack(Material.CLOCK);
        setDisplayNameAndLore(item, Component.text(label, NamedTextColor.YELLOW), null);
        layer.setItem(slot, GuiItem.builder()
                .item(item)
                .onClick((p, ctx) -> {
                    p.closeInventory();
                    p.performCommand("mp " + type + " " + target.getName() + " " + durationString + " Punishment via GUI");
                })
                .build());
    }
}
