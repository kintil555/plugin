package com.randomskill.skills;

import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerSkillData {

    private final UUID playerUUID;
    private SkillType currentSkill;
    private UUID selectedTarget;
    private long skillReceivedTime;
    private long lastUsedTime;
    private boolean skillUsed;

    // Freeze tracking
    private boolean frozen;

    public PlayerSkillData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.currentSkill = null;
        this.selectedTarget = null;
        this.skillReceivedTime = 0;
        this.lastUsedTime = 0;
        this.skillUsed = false;
        this.frozen = false;
    }

    public UUID getPlayerUUID() { return playerUUID; }

    public SkillType getCurrentSkill() { return currentSkill; }
    public void setCurrentSkill(SkillType skill) {
        this.currentSkill = skill;
        this.skillReceivedTime = System.currentTimeMillis();
        this.skillUsed = false;
    }

    public void clearSkill() {
        this.currentSkill = null;
        this.selectedTarget = null;
        this.skillUsed = true;
    }

    public UUID getSelectedTarget() { return selectedTarget; }
    public void setSelectedTarget(UUID target) { this.selectedTarget = target; }

    public long getSkillReceivedTime() { return skillReceivedTime; }

    public long getLastUsedTime() { return lastUsedTime; }
    public void setLastUsedTime(long time) { this.lastUsedTime = time; }

    public boolean isSkillUsed() { return skillUsed; }

    public boolean hasSkill() { return currentSkill != null && !skillUsed; }

    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public boolean isOnCooldown(long cooldownSeconds) {
        if (cooldownSeconds <= 0) return false;
        return (System.currentTimeMillis() - lastUsedTime) < (cooldownSeconds * 1000L);
    }

    public long getCooldownRemaining(long cooldownSeconds) {
        long elapsed = System.currentTimeMillis() - lastUsedTime;
        long remaining = (cooldownSeconds * 1000L) - elapsed;
        return Math.max(0, remaining / 1000L);
    }
}
