package org.yuemi.management.plugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.yuemi.libs.api.gui.Gui;
import org.yuemi.libs.api.gui.GuiItem;
import org.yuemi.libs.api.gui.GuiProvider;
import org.yuemi.management.plugin.ManagementPanelPlugin;

import java.util.List;

public final class PlayerActionGui extends BasePanelGui {

    public PlayerActionGui(@NotNull ManagementPanelPlugin plugin) {
        super(plugin);
    }

    public void open(@NotNull Player viewer, @NotNull OfflinePlayer target) {
        var api = GuiProvider.getApi();
        if (api == null) return;

        Gui gui = api.createBuilder()
                .title("Actions: " + target.getName())
                .rows(4)
                .createLayer("background", 0, layer -> layer.fill(createFiller()))
                .createLayer("actions", 1, layer -> {
                    ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
                    targetHead.editMeta(SkullMeta.class, skullMeta -> {
                        skullMeta.setOwningPlayer(target);
                        skullMeta.displayName(Component.text(target.getName() != null ? target.getName() : "Unknown", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false));
                        skullMeta.lore(List.of(
                                Component.text("UUID: " + target.getUniqueId(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                                Component.text("Status: " + (target.isOnline() ? "ONLINE" : "OFFLINE"), target.isOnline() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
                        ));
                    });
                    layer.setItem(4, GuiItem.builder().item(targetHead).build());

                    // Kick (slot 19)
                    ItemStack kickItem = new ItemStack(Material.LEATHER_BOOTS);
                    boolean hasKick = pHasPermission(viewer, "kick");
                    setDisplayNameAndLore(kickItem,
                            Component.text("Kick Player", hasKick ? NamedTextColor.YELLOW : NamedTextColor.RED),
                            List.of(
                                    Component.text("Instantly kick this player from the server", NamedTextColor.GRAY),
                                    Component.text(hasKick ? "Allowed" : "LOCKED: Needs kick permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(19, GuiItem.builder()
                            .item(kickItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "kick")) {
                                    p.closeInventory();
                                    p.performCommand("mp kick " + target.getName() + " Kicked via GUI");
                                }
                            })
                            .build());

                    // Mute (slot 20)
                    ItemStack muteItem = new ItemStack(Material.FEATHER);
                    boolean hasMute = pHasPermission(viewer, "mute");
                    setDisplayNameAndLore(muteItem,
                            Component.text("Mute Player", hasMute ? NamedTextColor.YELLOW : NamedTextColor.RED),
                            List.of(
                                    Component.text("Mute this player's chat", NamedTextColor.GRAY),
                                    Component.text(hasMute ? "Allowed" : "LOCKED: Needs mute permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(20, GuiItem.builder()
                            .item(muteItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "mute")) {
                                    new PunishDurationGui(plugin).open(p, target, "mute");
                                }
                            })
                            .build());

                    // Unmute (slot 21)
                    ItemStack unmuteItem = new ItemStack(Material.SHEARS);
                    boolean hasUnmute = pHasPermission(viewer, "unmute");
                    setDisplayNameAndLore(unmuteItem,
                            Component.text("Unmute Player", hasUnmute ? NamedTextColor.GREEN : NamedTextColor.RED),
                            List.of(
                                    Component.text("Remove mute from this player", NamedTextColor.GRAY),
                                    Component.text(hasUnmute ? "Allowed" : "LOCKED: Needs unmute permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(21, GuiItem.builder()
                            .item(unmuteItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "unmute")) {
                                    p.closeInventory();
                                    p.performCommand("mp unmute " + target.getName());
                                }
                            })
                            .build());

                    // Ban (slot 22)
                    ItemStack banItem = new ItemStack(Material.BARRIER);
                    boolean hasBan = pHasPermission(viewer, "ban");
                    setDisplayNameAndLore(banItem,
                            Component.text("Ban Player", hasBan ? NamedTextColor.DARK_RED : NamedTextColor.RED),
                            List.of(
                                    Component.text("Ban this player from the server", NamedTextColor.GRAY),
                                    Component.text(hasBan ? "Allowed" : "LOCKED: Needs ban permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(22, GuiItem.builder()
                            .item(banItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "ban")) {
                                    new PunishDurationGui(plugin).open(p, target, "ban");
                                }
                            })
                            .build());

                    // Unban (slot 23)
                    ItemStack unbanItem = new ItemStack(Material.ANVIL);
                    boolean hasUnban = pHasPermission(viewer, "unban");
                    setDisplayNameAndLore(unbanItem,
                            Component.text("Unban Player", hasUnban ? NamedTextColor.GREEN : NamedTextColor.RED),
                            List.of(
                                    Component.text("Pardon this player's ban", NamedTextColor.GRAY),
                                    Component.text(hasUnban ? "Allowed" : "LOCKED: Needs unban permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(23, GuiItem.builder()
                            .item(unbanItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "unban")) {
                                    p.closeInventory();
                                    p.performCommand("mp unban " + target.getName());
                                }
                            })
                            .build());

                    // Warn (slot 24)
                    ItemStack warnItem = new ItemStack(Material.WRITABLE_BOOK);
                    boolean hasWarn = pHasPermission(viewer, "warn");
                    setDisplayNameAndLore(warnItem,
                            Component.text("Warn Player", hasWarn ? NamedTextColor.GOLD : NamedTextColor.RED),
                            List.of(
                                    Component.text("Issue a formal warning", NamedTextColor.GRAY),
                                    Component.text(hasWarn ? "Allowed" : "LOCKED: Needs warn permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(24, GuiItem.builder()
                            .item(warnItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "warn")) {
                                    p.closeInventory();
                                    p.performCommand("mp warn " + target.getName() + " Warned via GUI");
                                }
                            })
                            .build());

                    // Wipe (slot 25)
                    ItemStack wipeItem = new ItemStack(Material.LAVA_BUCKET);
                    boolean hasWipe = pHasPermission(viewer, "wipe");
                    setDisplayNameAndLore(wipeItem,
                            Component.text("Wipe Player Data", hasWipe ? NamedTextColor.RED : NamedTextColor.DARK_RED),
                            List.of(
                                    Component.text("Completely wipe player data (destructive!)", NamedTextColor.GRAY),
                                    Component.text(hasWipe ? "Allowed" : "LOCKED: Needs wipe permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(25, GuiItem.builder()
                            .item(wipeItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "wipe")) {
                                    new WipeConfirmGui(plugin).open(p, target);
                                }
                            })
                            .build());

                    // Unwipe (slot 26)
                    ItemStack unwipeItem = new ItemStack(Material.WATER_BUCKET);
                    boolean hasUnwipe = pHasPermission(viewer, "unwipe");
                    setDisplayNameAndLore(unwipeItem,
                            Component.text("Restore Player Data", hasUnwipe ? NamedTextColor.AQUA : NamedTextColor.RED),
                            List.of(
                                    Component.text("Restore from latest wipe backup", NamedTextColor.GRAY),
                                    Component.text(hasUnwipe ? "Allowed" : "LOCKED: Needs unwipe permission", NamedTextColor.DARK_GRAY)
                            ));
                    layer.setItem(26, GuiItem.builder()
                            .item(unwipeItem)
                            .onClick((p, ctx) -> {
                                if (checkPermission(p, "unwipe")) {
                                    p.closeInventory();
                                    p.performCommand("mp unwipe " + target.getName());
                                }
                            })
                            .build());

                    // Back button
                    ItemStack backArrow = new ItemStack(Material.ARROW);
                    setDisplayNameAndLore(backArrow, Component.text("Back to Player List", NamedTextColor.GRAY), null);
                    layer.setItem(31, GuiItem.builder()
                            .item(backArrow)
                            .onClick((p, ctx) -> new PlayerListGui(plugin).open(p, 0))
                            .build());
                })
                .build();

        gui.open(viewer);
    }
}
