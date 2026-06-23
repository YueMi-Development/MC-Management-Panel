package org.yuemi.management.plugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yuemi.libs.api.gui.GuiItem;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.util.List;

public abstract class BasePanelGui {

    protected final ManagementPanelPlugin plugin;

    protected BasePanelGui(@NotNull ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    protected void setDisplayNameAndLore(ItemStack item, Component displayName, List<Component> lore) {
        item.editMeta(meta -> {
            meta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
            if (lore != null && !lore.isEmpty()) {
                List<Component> noItalicLore = lore.stream()
                        .map(c -> c.decoration(TextDecoration.ITALIC, false))
                        .toList();
                meta.lore(noItalicLore);
            }
        });
    }

    protected GuiItem createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        setDisplayNameAndLore(item, Component.text(" "), null);
        return GuiItem.builder()
                .item(item)
                .build();
    }

    protected boolean pHasPermission(@NotNull Player player, @NotNull String perm) {
        return player.hasPermission(perm);
    }

    protected boolean checkPermission(@NotNull Player player, @NotNull String perm) {
        if (!player.hasPermission(perm)) {
            player.sendMessage(Component.text("You do not have permission: " + perm, NamedTextColor.RED));
            return false;
        }
        return true;
    }
}
