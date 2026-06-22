package org.yuemi.management.plugin.command.sub;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SubCommand {

    @NotNull
    String getName();

    @NotNull
    String getPermission();

    void execute(@NotNull CommandSender sender, @NotNull String[] args);

    @Nullable
    List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}
