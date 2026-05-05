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
import java.util.Arrays;
import java.util.List;

public class SkillCommand implements CommandExecutor, TabCompleter {

    private final RandomSkillPlugin plugin;
    private final SkillManager skillManager;

    public SkillCommand(RandomSkillPlugin plugin, SkillManager skillManager) {
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

        // /skill (tanpa args) → tampilkan info skill
        if (args.length == 0) {
            showSkillInfo(player, prefix);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "use", "pakai" -> skillManager.useSkill(player);
            case "info" -> showSkillInfo(player, prefix);
            default -> {
                player.sendMessage(color(prefix + "&cCommand tidak dikenal! Gunakan &e/skill &catau &e/skill use"));
            }
        }

        return true;
    }

    private void showSkillInfo(Player player, String prefix) {
        PlayerSkillData data = skillManager.getData(player);

        player.sendMessage(color("&8&m---------------------------"));
        player.sendMessage(color("  &6&lRandomSkill &7- Skill Kamu"));
        player.sendMessage(color("&8&m---------------------------"));

        if (!data.hasSkill()) {
            player.sendMessage(color("  &cKamu tidak punya skill aktif."));
            player.sendMessage(color("  &7Bunuh mob untuk mendapatkan skill!"));
        } else {
            SkillType skill = data.getCurrentSkill();
            player.sendMessage(color("  &7Skill Aktif: " + skill.getDisplayName()));
            player.sendMessage(color("  &7Deskripsi: &f" + skill.getDescription()));

            if (skill.requiresTarget()) {
                player.sendMessage(color("  &7Tipe: &cSkill Target &7(butuh &e/select <player>&7)"));
                if (data.getSelectedTarget() != null) {
                    Player target = Bukkit.getPlayer(data.getSelectedTarget());
                    String targetName = target != null ? target.getName() : "Offline";
                    player.sendMessage(color("  &7Target: &e" + targetName));
                } else {
                    player.sendMessage(color("  &7Target: &cBelum dipilih"));
                }
            } else {
                player.sendMessage(color("  &7Tipe: &aSelf Skill"));
            }

            player.sendMessage(color("  &7Gunakan: &b/skill use"));
        }

        // Cooldown info
        long cooldown = plugin.getConfig().getLong("skill-cooldown", 30);
        if (data.isOnCooldown(cooldown)) {
            long remaining = data.getCooldownRemaining(cooldown);
            player.sendMessage(color("  &7Cooldown: &c" + remaining + " &7detik tersisa"));
        } else {
            player.sendMessage(color("  &7Cooldown: &aReady!"));
        }

        player.sendMessage(color("&8&m---------------------------"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("use", "info");
        }
        return new ArrayList<>();
    }

    private Component color(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }
}
