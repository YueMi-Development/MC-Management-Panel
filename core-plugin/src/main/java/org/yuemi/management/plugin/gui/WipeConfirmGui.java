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
import org.yuemi.libs.api.gui.GuiProvider;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.util.List;

public final class WipeConfirmGui extends BasePanelGui {

    public WipeConfirmGui(@NotNull ManagementPanelPlugin plugin) {
        super(plugin);
    }

    public void open(@NotNull Player viewer, @NotNull OfflinePlayer target) {
        var api = GuiProvider.getApi();
        if (api == null) return;

        Gui gui = api.createBuilder()
                .title("Wipe: " + target.getName() + "?")
                .rows(3)
                .createLayer("background", 0, layer -> layer.fill(createFiller()))
                .createLayer("confirm", 1, layer -> {
                    ItemStack yesBtn = new ItemStack(Material.LIME_CONCRETE);
                    setDisplayNameAndLore(yesBtn, Component.text("CONFIRM WIPE", NamedTextColor.GREEN),
                            List.of(
                                    Component.text("WARNING: This will permanently delete all", NamedTextColor.RED),
                                    Component.text("player inventory, ender chest, and economy data!", NamedTextColor.RED)
                            ));
                    layer.setItem(11, GuiItem.builder()
                            .item(yesBtn)
                            .onClick((p, ctx) -> {
                                p.closeInventory();
                                p.performCommand("mp wipe " + target.getName());
                            })
                            .build());

                    ItemStack noBtn = new ItemStack(Material.RED_CONCRETE);
                    setDisplayNameAndLore(noBtn, Component.text("CANCEL", NamedTextColor.RED), null);
                    layer.setItem(15, GuiItem.builder()
                            .item(noBtn)
                            .onClick((p, ctx) -> new PlayerActionGui(plugin).open(p, target))
                            .build());
                })
                .build();

        gui.open(viewer);
    }
}
