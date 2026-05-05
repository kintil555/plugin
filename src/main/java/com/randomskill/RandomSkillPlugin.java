package com.randomskill;

import com.randomskill.commands.SelectCommand;
import com.randomskill.commands.SkillCommand;
import com.randomskill.commands.SkillReloadCommand;
import com.randomskill.listeners.FreezeListener;
import com.randomskill.listeners.MobKillListener;
import com.randomskill.managers.SkillManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomSkillPlugin extends JavaPlugin {

    private SkillManager skillManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Init manager
        skillManager = new SkillManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new MobKillListener(this, skillManager), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this, skillManager), this);

        // Register commands
        getCommand("select").setExecutor(new SelectCommand(this, skillManager));
        getCommand("select").setTabCompleter(new SelectCommand(this, skillManager));

        getCommand("skill").setExecutor(new SkillCommand(this, skillManager));
        getCommand("skill").setTabCompleter(new SkillCommand(this, skillManager));

        getCommand("skillreload").setExecutor(new SkillReloadCommand(this));

        getLogger().info("=================================");
        getLogger().info("  RandomSkillPlugin v" + getDescription().getVersion() + " Aktif!");
        getLogger().info("  Skill count: " + com.randomskill.skills.SkillType.values().length);
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("RandomSkillPlugin dimatikan!");
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }
}
