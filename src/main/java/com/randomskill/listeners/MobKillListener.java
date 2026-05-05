package com.randomskill.listeners;

import com.randomskill.RandomSkillPlugin;
import com.randomskill.managers.SkillManager;
import com.randomskill.skills.PlayerSkillData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements Listener {

    private final RandomSkillPlugin plugin;
    private final SkillManager skillManager;

    public MobKillListener(RandomSkillPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobKill(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Hanya berlaku jika yang mati adalah mob (bukan player)
        if (entity instanceof Player) return;

        // Hanya berlaku jika yang membunuh adalah player
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        // Hanya mob (Monster untuk hostile mobs, atau semua entity non-player)
        // Uncomment baris bawah jika ingin hanya hostile mobs:
        // if (!(entity instanceof Monster)) return;

        // Cek permission
        if (!killer.hasPermission("randomskill.use")) return;

        // Roll skill
        skillManager.rollSkill(killer);
    }
}
