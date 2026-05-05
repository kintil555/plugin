package com.randomskill.commands;

import com.randomskill.RandomSkillPlugin;
import com.randomskill.managers.SkillManager;
import com.randomskill.skills.PlayerSkillData;
import com.randomskill.skills.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SelectCommand implements CommandExecutor, TabCompleter {

    private final RandomSkillPlugin plugin;
    private final SkillManager skillManager;

    public SelectCommand(RandomSkillPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya player yang bisa menggunakan command ini!");
            return true;
        }

        String prefix = plugin.getConfig().getString("prefix", "&8[&6RandomSkill&8] ");

        if (args.length < 1) {
            player.sendMessage(color(prefix + "&cUsage: /select <nama_player>"));
            return true;
        }

        PlayerSkillData data = skillManager.getData(player);

        // Cek apakah player punya skill yang butuh target
        if (!data.hasSkill()) {
            player.sendMessage(color(prefix + "&cKamu tidak punya skill aktif yang membutuhkan target!"));
            return true;
        }

        SkillType skill = data.getCurrentSkill();
        if (!skill.requiresTarget()) {
            player.sendMessage(color(prefix + "&cSkill kamu (&e" + skill.getDisplayName() + "&c) tidak membutuhkan target!"));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null || !target.isOnline()) {
            player.sendMessage(color(prefix + "&cPlayer &e" + targetName + " &ctidak ditemukan atau tidak online!"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(color(prefix + "&cKamu tidak bisa menarget dirimu sendiri!"));
            return true;
        }

        skillManager.setTarget(player, target);

        // Auto-prompt untuk langsung pakai
        player.sendMessage(color(prefix + "&eKetik &b/skill use &euntuk menggunakan skill pada &b" + target.getName() + "&e!"));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(sender) && online.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(online.getName());
                }
            }
        }
        return completions;
    }

    private Component color(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }
}
