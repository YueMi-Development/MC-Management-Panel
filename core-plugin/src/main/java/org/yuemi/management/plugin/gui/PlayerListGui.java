package org.yuemi.management.plugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.yuemi.libs.api.gui.Gui;
import org.yuemi.libs.api.gui.GuiItem;
import org.yuemi.libs.api.gui.GuiProvider;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.util.ArrayList;
import java.util.List;

public final class PlayerListGui extends BasePanelGui {

    public PlayerListGui(@NotNull ManagementPanelPlugin plugin) {
        super(plugin);
    }

    public void open(@NotNull Player player, int page) {
        var api = GuiProvider.getApi();
        if (api == null) return;

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int playersPerPage = 45;
        int maxPages = (int) Math.ceil((double) onlinePlayers.size() / playersPerPage);
        if (maxPages == 0) maxPages = 1;
        final int finalPage = Math.min(page, maxPages - 1);

        Gui gui = api.createBuilder()
                .title("Manage Players - Page " + (finalPage + 1) + "/" + maxPages)
                .rows(6)
                .createLayer("background", 0, layer -> {
                    for (int i = 45; i < 54; i++) {
                        layer.setItem(i, createFiller());
                    }
                })
                .createLayer("players", 1, layer -> {
                    int startIndex = finalPage * playersPerPage;
                    int endIndex = Math.min(startIndex + playersPerPage, onlinePlayers.size());

                    for (int i = startIndex; i < endIndex; i++) {
                        Player target = onlinePlayers.get(i);
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        head.editMeta(SkullMeta.class, skullMeta -> {
                            skullMeta.setOwningPlayer(target);
                            skullMeta.displayName(Component.text(target.getName(), NamedTextColor.GOLD)
                                    .decoration(TextDecoration.ITALIC, false));
                            skullMeta.lore(List.of(
                                    Component.text("UUID: " + target.getUniqueId(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                                    Component.text("Click to open action menu", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                            ));
                        });

                        int slot = i - startIndex;
                        layer.setItem(slot, GuiItem.builder()
                                .item(head)
                                .onClick((p, ctx) -> new PlayerActionGui(plugin).open(p, target))
                                .build());
                    }

                    ItemStack backArrow = new ItemStack(Material.ARROW);
                    setDisplayNameAndLore(backArrow, Component.text("Back to Main Menu", NamedTextColor.GRAY), null);
                    layer.setItem(45, GuiItem.builder()
                            .item(backArrow)
                            .onClick((p, ctx) -> new MainMenuGui(plugin).open(p))
                            .build());

                    if (finalPage > 0) {
                        ItemStack prevPage = new ItemStack(Material.FEATHER);
                        setDisplayNameAndLore(prevPage, Component.text("Previous Page", NamedTextColor.YELLOW), null);
                        layer.setItem(48, GuiItem.builder()
                                .item(prevPage)
                                .onClick((p, ctx) -> open(p, finalPage - 1))
                                .build());
                    }

                    if (endIndex < onlinePlayers.size()) {
                        ItemStack nextPage = new ItemStack(Material.FEATHER);
                        setDisplayNameAndLore(nextPage, Component.text("Next Page", NamedTextColor.YELLOW), null);
                        layer.setItem(50, GuiItem.builder()
                                .item(nextPage)
                                .onClick((p, ctx) -> open(p, finalPage + 1))
                                .build());
                    }
                })
                .build();

        gui.open(player);
    }
}
