package com.randomskill.listeners;

import com.randomskill.RandomSkillPlugin;
import com.randomskill.managers.SkillManager;
import com.randomskill.skills.PlayerSkillData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.Location;

public class FreezeListener implements Listener {

    private final RandomSkillPlugin plugin;
    private final SkillManager skillManager;

    public FreezeListener(RandomSkillPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerSkillData data = skillManager.getData(player);

        if (!data.isFrozen()) return;

        // Cegah pergerakan XYZ tapi izinkan kepala rotate
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        // Jika posisi XZ berubah, teleport balik
        if (from.getBlockX() != to.getBlockX() ||
            from.getBlockY() != to.getBlockY() ||
            from.getBlockZ() != to.getBlockZ()) {

            // Kembalikan ke posisi asal, tapi izinkan head rotation
            Location frozenLoc = from.clone();
            frozenLoc.setYaw(to.getYaw());
            frozenLoc.setPitch(to.getPitch());
            event.setTo(frozenLoc);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Izinkan teleport yang dipanggil oleh plugin (void drop, dll)
        // Tapi cegah teleport biasa saat freeze
        Player player = event.getPlayer();
        PlayerSkillData data = skillManager.getData(player);

        if (!data.isFrozen()) return;

        // Cek cause - izinkan teleport dari command/plugin
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerSkillData data = skillManager.getData(player);
        // Unfreeze saat keluar
        if (data.isFrozen()) {
            data.setFrozen(false);
        }
    }
}
