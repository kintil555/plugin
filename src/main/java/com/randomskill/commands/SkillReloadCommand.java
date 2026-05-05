package com.randomskill.commands;

import com.randomskill.RandomSkillPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SkillReloadCommand implements CommandExecutor {

    private final RandomSkillPlugin plugin;

    public SkillReloadCommand(RandomSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("randomskill.admin")) {
            sender.sendMessage(color("&cKamu tidak punya permission untuk ini!"));
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(color(plugin.getConfig().getString("prefix", "&8[&6RandomSkill&8] ")
                + "&aKonfigurasi berhasil di-reload!"));
        return true;
    }

    private Component color(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }
}
