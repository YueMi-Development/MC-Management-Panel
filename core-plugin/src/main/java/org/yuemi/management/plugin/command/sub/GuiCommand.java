package org.yuemi.management.plugin.command.sub;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yuemi.management.plugin.ManagementPanelPlugin;
import org.yuemi.management.plugin.command.helper.CommandHelper;
import org.yuemi.management.plugin.gui.MainMenuGui;

import java.util.Collections;
import java.util.List;

public class GuiCommand implements SubCommand {

    private final ManagementPanelPlugin plugin;

    public GuiCommand(ManagementPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return "gui";
    }

    @Override
    public @NotNull String getPermission() {
        return "managementpanel.gui";
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            CommandHelper.sendMsg(sender, "Only players can open the GUI.", NamedTextColor.RED);
            return;
        }

        new MainMenuGui(plugin).open(player);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
