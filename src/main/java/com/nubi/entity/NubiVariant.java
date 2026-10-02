package com.nubi.entity;

import net.minecraft.util.RandomSource;

public enum NubiVariant {
    WHITE(0, "white"),
    PINK(1, "pink"),
    BLUE(2, "blue"),
    YELLOW(3, "yellow");

    private final int id;
    private final String name;

    NubiVariant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public static NubiVariant byId(int id) {
        for (NubiVariant v : values()) {
            if (v.id == id) return v;
        }
        return WHITE;
    }

    /** Rosa, azul o amarilla (solo en el bioma Mundo Nubi). */
    public static NubiVariant randomColored(RandomSource random) {
        return values()[1 + random.nextInt(3)];
    }
}
