package com.randomskill.skills;

public enum SkillType {

    // ===== SKILL SELF (tidak butuh target) =====
    BUFF_EFFECTS(
        "Aura Petarung",
        "&6⚔ Aura Petarung",
        "Mendapat Haste II, Strength II, Regen II & Instant Health selama 4 menit!",
        false,
        SkillCategory.SELF
    ),

    TOTEM(
        "Totem Kehidupan",
        "&aTotem Kehidupan",
        "Mendapat Totem of Undying di inventori!",
        false,
        SkillCategory.SELF
    ),

    FOOD_BUNDLE(
        "Berkah Makanan",
        "&eBerkah Makanan",
        "Mendapat berbagai makanan lezat!",
        false,
        SkillCategory.SELF
    ),

    INSTANT_HEAL(
        "Sembuh Instan",
        "&cSembuh Instan",
        "Langsung pulih penuh! HP & Hunger terisi penuh!",
        false,
        SkillCategory.SELF
    ),

    SPEED_BOOST(
        "Angin Kilat",
        "&bAngin Kilat",
        "Mendapat Speed III selama 3 menit!",
        false,
        SkillCategory.SELF
    ),

    INVISIBILITY(
        "Bayang Gelap",
        "&8Bayang Gelap",
        "Mendapat Invisibility selama 2 menit!",
        false,
        SkillCategory.SELF
    ),

    FIRE_RESISTANCE(
        "Tubuh Api",
        "&cTubuh Api",
        "Mendapat Fire Resistance & Resistance selama 5 menit!",
        false,
        SkillCategory.SELF
    ),

    LUCKY_LOOT(
        "Jarahan Beruntung",
        "&6Jarahan Beruntung",
        "Mendapat item random berharga dari chest looting!",
        false,
        SkillCategory.SELF
    ),

    JUMP_BOOST(
        "Lompatan Dewa",
        "&dLompatan Dewa",
        "Mendapat Jump Boost V selama 2 menit!",
        false,
        SkillCategory.SELF
    ),

    NIGHT_VISION(
        "Mata Elang",
        "&eMata Elang",
        "Mendapat Night Vision & Glowing pada semua musuh selama 5 menit!",
        false,
        SkillCategory.SELF
    ),

    // ===== SKILL TARGET (butuh /select player) =====
    FREEZE(
        "Pembekuan",
        "&bPembekuan",
        "Membekukan player lain selama 6 detik! Mereka tidak bisa bergerak.",
        true,
        SkillCategory.TARGET
    ),

    VOID_DROP(
        "Jatuh ke Void",
        "&0Jatuh ke Void",
        "Menjatuhkan player lain ke void! (Y = -64)",
        true,
        SkillCategory.TARGET
    ),

    STEAL_INVENTORY(
        "Pencuri Bayangan",
        "&5Pencuri Bayangan",
        "Mencuri beberapa item dari inventori player lain!",
        true,
        SkillCategory.TARGET
    ),

    DARKNESS_VISION(
        "Kabut Kegelapan",
        "&8Kabut Kegelapan",
        "Membuat visual player lain menjadi sangat gelap selama 10 detik!",
        true,
        SkillCategory.TARGET
    ),

    LIGHTNING_STRIKE(
        "Petir Kutukan",
        "&ePetir Kutukan",
        "Memanggil petir ke posisi player target!",
        true,
        SkillCategory.TARGET
    ),

    POISON_CURSE(
        "Kutukan Racun",
        "&2Kutukan Racun",
        "Memberikan Poison III & Slowness III ke player target selama 30 detik!",
        true,
        SkillCategory.TARGET
    ),

    CONFUSION(
        "Kebingungan",
        "&dKebingungan",
        "Memberikan Nausea & Blindness ke player target selama 15 detik!",
        true,
        SkillCategory.TARGET
    ),

    LEVITATION_TRAP(
        "Perangkap Melayang",
        "&fPerangkap Melayang",
        "Membuat player target melayang tinggi lalu jatuh!",
        true,
        SkillCategory.TARGET
    );

    private final String name;
    private final String displayName;
    private final String description;
    private final boolean requiresTarget;
    private final SkillCategory category;

    SkillType(String name, String displayName, String description,
              boolean requiresTarget, SkillCategory category) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.requiresTarget = requiresTarget;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean requiresTarget() { return requiresTarget; }
    public SkillCategory getCategory() { return category; }

    public enum SkillCategory {
        SELF, TARGET
    }
}
