package com.randomskill.managers;

import com.randomskill.RandomSkillPlugin;
import com.randomskill.skills.PlayerSkillData;
import com.randomskill.skills.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class SkillManager {

    private final RandomSkillPlugin plugin;
    private final Map<UUID, PlayerSkillData> playerDataMap = new HashMap<>();
    private final Random random = new Random();

    // Daftar skill yang bisa didapat
    private static final SkillType[] ALL_SKILLS = SkillType.values();

    public SkillManager(RandomSkillPlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== DATA MANAGEMENT ====================

    public PlayerSkillData getData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), PlayerSkillData::new);
    }

    public void removeData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    // ==================== SKILL ROLL ====================

    public void rollSkill(Player player) {
        int chance = plugin.getConfig().getInt("skill-chance", 40);
        if (random.nextInt(100) >= chance) return;

        PlayerSkillData data = getData(player);

        // Ganti skill lama jika belum dipakai (opsional: bisa diubah ke queue)
        SkillType newSkill = ALL_SKILLS[random.nextInt(ALL_SKILLS.length)];
        data.setCurrentSkill(newSkill);
        data.setSelectedTarget(null);

        // Notify player
        String msg = plugin.getConfig().getString("messages.skill-received", "&aKamu mendapat skill: &e%skill%")
                .replace("%skill%", newSkill.getDisplayName());
        player.sendMessage(color(plugin.getConfig().getString("prefix", "&8[&6RandomSkill&8] ") + msg));

        // Title notif
        showSkillTitle(player, newSkill);

        // Sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        // Jika skill targetted, minta pilih player
        if (newSkill.requiresTarget()) {
            player.sendMessage(color(plugin.getConfig().getString("prefix", "&8[&6RandomSkill&8] ")
                    + "&eSkill ini membutuhkan target! Ketik &b/select <nama_player> &elalu gunakan &b/skill use"));
        } else {
            player.sendMessage(color(plugin.getConfig().getString("prefix", "&8[&6RandomSkill&8] ")
                    + "&eKetik &b/skill use &euntuk menggunakan skill!"));
        }
    }

    private void showSkillTitle(Player player, SkillType skill) {
        Title title = Title.title(
                color(skill.getDisplayName()),
                color("&7" + skill.getDescription()),
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(3),
                        Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
    }

    // ==================== SKILL USE ====================

    public void useSkill(Player player) {
        PlayerSkillData data = getData(player);
        String prefix = plugin.getConfig().getString("prefix", "&8[&6RandomSkill&8] ");

        if (!data.hasSkill()) {
            player.sendMessage(color(prefix + plugin.getConfig().getString("messages.no-skill", "&cKamu tidak punya skill aktif!")));
            return;
        }

        long cooldown = plugin.getConfig().getLong("skill-cooldown", 30);
        if (data.isOnCooldown(cooldown)) {
            long remaining = data.getCooldownRemaining(cooldown);
            String msg = plugin.getConfig().getString("messages.cooldown", "&cSkill masih cooldown! &e%time% &cdetik lagi.")
                    .replace("%time%", String.valueOf(remaining));
            player.sendMessage(color(prefix + msg));
            return;
        }

        SkillType skill = data.getCurrentSkill();

        // Cek apakah butuh target
        if (skill.requiresTarget()) {
            if (data.getSelectedTarget() == null) {
                player.sendMessage(color(prefix + plugin.getConfig().getString("messages.select-required",
                        "&cSkill ini membutuhkan target! Gunakan &e/select <player>")));
                return;
            }

            Player target = Bukkit.getPlayer(data.getSelectedTarget());
            if (target == null || !target.isOnline()) {
                player.sendMessage(color(prefix + plugin.getConfig().getString("messages.target-offline", "&cPlayer target tidak online!")));
                data.setSelectedTarget(null);
                return;
            }

            executeTargetSkill(player, target, skill);
        } else {
            executeSelfSkill(player, skill);
        }

        // Post-use
        data.setLastUsedTime(System.currentTimeMillis());
        data.clearSkill();

        String usedMsg = plugin.getConfig().getString("messages.skill-used", "&aSkill &e%skill% &atelah digunakan!")
                .replace("%skill%", skill.getDisplayName());
        player.sendMessage(color(prefix + usedMsg));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    // ==================== SELF SKILLS ====================

    private void executeSelfSkill(Player player, SkillType skill) {
        switch (skill) {
            case BUFF_EFFECTS -> skillBuffEffects(player);
            case TOTEM -> skillTotem(player);
            case FOOD_BUNDLE -> skillFoodBundle(player);
            case INSTANT_HEAL -> skillInstantHeal(player);
            case SPEED_BOOST -> skillSpeedBoost(player);
            case INVISIBILITY -> skillInvisibility(player);
            case FIRE_RESISTANCE -> skillFireResistance(player);
            case LUCKY_LOOT -> skillLuckyLoot(player);
            case JUMP_BOOST -> skillJumpBoost(player);
            case NIGHT_VISION -> skillNightVision(player);
            default -> player.sendMessage(color("&cSkill tidak dikenali."));
        }
    }

    private void skillBuffEffects(Player player) {
        int dur = plugin.getConfig().getInt("skill-duration.buff-effects", 240) * 20;
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, dur, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, dur, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, dur, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, dur, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1));
        spawnParticles(player, Particle.HEART, 20);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
    }

    private void skillTotem(Player player) {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);
        giveOrDrop(player, totem);
        spawnParticles(player, Particle.TOTEM_OF_UNDYING, 30);
    }

    private void skillFoodBundle(Player player) {
        Material[] foods = {
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.GOLDEN_APPLE,
            Material.BREAD, Material.COOKED_CHICKEN, Material.PUMPKIN_PIE,
            Material.CAKE, Material.GOLDEN_CARROT
        };
        for (Material food : foods) {
            giveOrDrop(player, new ItemStack(food, 3 + random.nextInt(5)));
        }
        spawnParticles(player, Particle.HAPPY_VILLAGER, 20);
    }

    private void skillInstantHeal(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
        spawnParticles(player, Particle.HEART, 30);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
    }

    private void skillSpeedBoost(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 180 * 20, 2));
        spawnParticles(player, Particle.CLOUD, 20);
    }

    private void skillInvisibility(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120 * 20, 0));
        spawnParticles(player, Particle.SMOKE, 20);
    }

    private void skillFireResistance(Player player) {
        int dur = 300 * 20;
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, dur, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, dur, 1));
        spawnParticles(player, Particle.FLAME, 30);
    }

    private void skillLuckyLoot(Player player) {
        Material[] loot = {
            Material.DIAMOND, Material.DIAMOND_SWORD, Material.DIAMOND_CHESTPLATE,
            Material.EMERALD, Material.NETHERITE_INGOT, Material.ENCHANTED_GOLDEN_APPLE,
            Material.ENDER_PEARL, Material.BLAZE_ROD, Material.GHAST_TEAR
        };
        int count = 3 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            Material mat = loot[random.nextInt(loot.length)];
            giveOrDrop(player, new ItemStack(mat, 1 + random.nextInt(3)));
        }
        spawnParticles(player, Particle.HAPPY_VILLAGER, 40);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 0.8f);
    }

    private void skillJumpBoost(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 120 * 20, 4));
        spawnParticles(player, Particle.HAPPY_VILLAGER, 15);
    }

    private void skillNightVision(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300 * 20, 0));
        // Tambah glowing ke semua player lain di dunia
        for (Player other : player.getWorld().getPlayers()) {
            if (!other.equals(player)) {
                other.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 300 * 20, 0));
            }
        }
        spawnParticles(player, Particle.END_ROD, 20);
    }

    // ==================== TARGET SKILLS ====================

    private void executeTargetSkill(Player caster, Player target, SkillType skill) {
        switch (skill) {
            case FREEZE -> skillFreeze(caster, target);
            case VOID_DROP -> skillVoidDrop(caster, target);
            case STEAL_INVENTORY -> skillStealInventory(caster, target);
            case DARKNESS_VISION -> skillDarkness(caster, target);
            case LIGHTNING_STRIKE -> skillLightning(caster, target);
            case POISON_CURSE -> skillPoisonCurse(caster, target);
            case CONFUSION -> skillConfusion(caster, target);
            case LEVITATION_TRAP -> skillLevitation(caster, target);
            default -> caster.sendMessage(color("&cSkill tidak dikenali."));
        }
    }

    private void skillFreeze(Player caster, Player target) {
        int freezeDur = plugin.getConfig().getInt("skill-duration.freeze", 6);
        PlayerSkillData targetData = getData(target);
        targetData.setFrozen(true);

        // Efek visual freeze
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, freezeDur * 20, 200, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, freezeDur * 20, -10, false, false));

        Location loc = target.getLocation();
        target.getWorld().spawnParticle(Particle.SNOWFLAKE, loc.add(0, 1, 0), 50, 0.5, 1, 0.5, 0.01);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 1f, 0.5f);

        target.sendMessage(color("&b❄ Kamu dibekukan oleh &e" + caster.getName() + " &bselama " + freezeDur + " detik!"));
        caster.sendMessage(color("&b❄ &e" + target.getName() + " &bberhasil dibekukan selama " + freezeDur + " detik!"));

        // Unfreeze setelah durasi
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline()) {
                    targetData.setFrozen(false);
                    target.removePotionEffect(PotionEffectType.SLOWNESS);
                    target.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    target.sendMessage(color("&a❄ Kamu sudah bisa bergerak kembali!"));
                    spawnParticles(target, Particle.CLOUD, 20);
                }
            }
        }.runTaskLater(plugin, freezeDur * 20L);
    }

    private void skillVoidDrop(Player caster, Player target) {
        Location voidLoc = target.getLocation().clone();
        voidLoc.setY(-60); // Void

        // Save mode gamemode dulu
        target.sendMessage(color("&0⬛ &cKamu dijatuhkan ke void oleh &e" + caster.getName() + "!"));
        caster.sendMessage(color("&0⬛ &e" + target.getName() + " &cberhasil dijatuhkan ke void!"));

        // Teleport ke void
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline()) {
                    target.teleport(voidLoc);
                    target.getWorld().strikeLightningEffect(voidLoc);
                    spawnParticles(target, Particle.PORTAL, 30);
                }
            }
        }.runTaskLater(plugin, 10L); // Delay kecil biar dramatic
    }

    private void skillStealInventory(Player caster, Player target) {
        List<ItemStack> stolenItems = new ArrayList<>();
        ItemStack[] inventory = target.getInventory().getContents();

        // Ambil 1-4 item random dari inventori target
        List<Integer> nonEmptySlots = new ArrayList<>();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].getType() != Material.AIR) {
                nonEmptySlots.add(i);
            }
        }

        if (nonEmptySlots.isEmpty()) {
            caster.sendMessage(color("&5👜 Inventori &e" + target.getName() + " &5kosong! Tidak ada yang bisa dicuri."));
            return;
        }

        int stealCount = Math.min(1 + random.nextInt(4), nonEmptySlots.size());
        Collections.shuffle(nonEmptySlots);

        StringBuilder stolenNames = new StringBuilder();
        for (int i = 0; i < stealCount; i++) {
            int slot = nonEmptySlots.get(i);
            ItemStack item = inventory[slot];
            stolenItems.add(item.clone());
            target.getInventory().setItem(slot, null);
            if (i > 0) stolenNames.append(", ");
            stolenNames.append(formatItemName(item.getType()));
        }

        // Berikan item ke caster
        for (ItemStack item : stolenItems) {
            giveOrDrop(caster, item);
        }

        target.sendMessage(color("&5👜 &e" + stealCount + " item &5dicuri dari inventori kamu oleh &e" + caster.getName() + "! (&c" + stolenNames + "&5)"));
        caster.sendMessage(color("&5👜 Berhasil mencuri &e" + stealCount + " item &5dari &e" + target.getName() + "!"));

        spawnParticles(caster, Particle.WITCH, 20);
        spawnParticles(target, Particle.WITCH, 20);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
    }

    private void skillDarkness(Player caster, Player target) {
        int dur = plugin.getConfig().getInt("skill-duration.darkness", 10);
        target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, dur * 20, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, dur * 20, 0));

        target.sendMessage(color("&8🌑 Visual kamu digelapkan oleh &e" + caster.getName() + " &8selama " + dur + " detik!"));
        caster.sendMessage(color("&8🌑 Visual &e" + target.getName() + " &8berhasil digelapkan!"));

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WARDEN_AMBIENT, 1f, 1f);
        spawnParticles(target, Particle.SMOKE, 30);
    }

    private void skillLightning(Player caster, Player target) {
        Location loc = target.getLocation();
        target.getWorld().strikeLightning(loc);
        target.getWorld().strikeLightning(loc);

        target.sendMessage(color("&e⚡ Kamu disambar petir oleh &e" + caster.getName() + "!"));
        caster.sendMessage(color("&e⚡ Petir berhasil dipanggil ke posisi &e" + target.getName() + "!"));
    }

    private void skillPoisonCurse(Player caster, Player target) {
        int dur = 30 * 20;
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dur, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, dur, 1));

        target.sendMessage(color("&2☠ Kamu dikutuk racun oleh &e" + caster.getName() + " &2selama 30 detik!"));
        caster.sendMessage(color("&2☠ Kutukan racun berhasil diberikan ke &e" + target.getName() + "!"));

        spawnParticles(target, Particle.SLIME, 25);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_THROW, 1f, 1f);
    }

    private void skillConfusion(Player caster, Player target) {
        int dur = 15 * 20;
        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, dur, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, dur, 0));

        target.sendMessage(color("&d🌀 Kamu dibuat kebingungan oleh &e" + caster.getName() + " &dselama 15 detik!"));
        caster.sendMessage(color("&d🌀 &e" + target.getName() + " &dberhasil dibuat kebingungan!"));

        spawnParticles(target, Particle.PORTAL, 30);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1f);
    }

    private void skillLevitation(Player caster, Player target) {
        // Levitate tinggi
        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 3, false, false)); // 3 detik melayang

        target.sendMessage(color("&f☁ Kamu diangkat ke langit oleh &e" + caster.getName() + "! &cBersiap jatuh!"));
        caster.sendMessage(color("&f☁ &e" + target.getName() + " &fberhasil diangkat ke langit!"));

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1f, 1.5f);
        spawnParticles(target, Particle.CLOUD, 30);

        // Setelah melayang, hentikan levitation (biar jatuh bebas)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline()) {
                    target.removePotionEffect(PotionEffectType.LEVITATION);
                    target.sendMessage(color("&c⬇ Kamu jatuh bebas!"));
                }
            }
        }.runTaskLater(plugin, 65L);
    }

    // ==================== UTILITY ====================

    public void setTarget(Player player, Player target) {
        PlayerSkillData data = getData(player);
        data.setSelectedTarget(target.getUniqueId());

        String prefix = plugin.getConfig().getString("prefix", "&8[&6RandomSkill&8] ");
        String msg = plugin.getConfig().getString("messages.select-set", "&aTarget dipilih: &e%player%")
                .replace("%player%", target.getName());
        player.sendMessage(color(prefix + msg));
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    private void spawnParticles(Player player, Particle particle, int count) {
        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), count, 0.5, 0.5, 0.5, 0.05);
    }

    private Component color(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    private String formatItemName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
