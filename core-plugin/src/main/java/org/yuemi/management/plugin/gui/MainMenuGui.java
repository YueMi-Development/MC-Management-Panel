package org.yuemi.management.plugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yuemi.libs.api.gui.Gui;
import org.yuemi.libs.api.gui.GuiItem;
import org.yuemi.libs.api.gui.GuiProvider;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.util.List;

public final class MainMenuGui extends BasePanelGui {

    public MainMenuGui(@NotNull ManagementPanelPlugin plugin) {
        super(plugin);
    }

    public void open(@NotNull Player player) {
        var api = GuiProvider.getApi();
        if (api == null) {
            player.sendMessage(Component.text("GUI API is not available at this moment.", NamedTextColor.RED));
            return;
        }

        Gui gui = api.createBuilder()
                .title("Management Panel Main")
                .rows(3)
                .createLayer("background", 0, layer -> layer.fill(createFiller()))
                .createLayer("buttons", 1, layer -> {
                    ItemStack playersItem = new ItemStack(Material.PLAYER_HEAD);
                    setDisplayNameAndLore(playersItem,
                            Component.text("Manage Players", NamedTextColor.GREEN),
                            List.of(Component.text("Click to view online players", NamedTextColor.GRAY)));
                    layer.setItem(11, GuiItem.builder()
                            .item(playersItem)
                            .onClick((p, ctx) -> new PlayerListGui(plugin).open(p, 0))
                            .build());

                    ItemStack statusItem = new ItemStack(Material.NETHER_STAR);
                    boolean hasStatusPerm = pHasPermission(player, "managementpanel.status");
                    setDisplayNameAndLore(statusItem,
                            Component.text("Server Status", hasStatusPerm ? NamedTextColor.YELLOW : NamedTextColor.RED),
                            List.of(
                                    Component.text("Click to view server details", NamedTextColor.GRAY),
                                    Component.text(hasStatusPerm ? "Status: Available" : "LOCKED: Needs managementpanel.status", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(13, GuiItem.builder()
                            .item(statusItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "managementpanel.status")) {
                                    p.closeInventory();
                                    p.performCommand("mp status");
                                }
                            })
                            .build());

                    ItemStack reloadItem = new ItemStack(Material.REDSTONE);
                    boolean hasReloadPerm = pHasPermission(player, "managementpanel.reload");
                    setDisplayNameAndLore(reloadItem,
                            Component.text("Reload Configuration", hasReloadPerm ? NamedTextColor.RED : NamedTextColor.DARK_RED),
                            List.of(
                                    Component.text("Click to reload configuration files", NamedTextColor.GRAY),
                                    Component.text(hasReloadPerm ? "Status: Available" : "LOCKED: Needs managementpanel.reload", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(15, GuiItem.builder()
                            .item(reloadItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "managementpanel.reload")) {
                                    p.closeInventory();
                                    p.performCommand("mp reload");
                                }
                            })
                            .build());
                })
                .build();

        gui.open(player);
    }
}
