package com.github.antigravity.kubejsstudio.mekanism.recipe;

/**
 * Supported Mekanism machine recipe types that involve gases.
 */
public enum MekanismGasRecipeType {
    CRYSTALLIZING("crystallizing", "mekanism:crystallizing", "kubejsstudio.mekanism.form.crystallizing"),
    DISSOLUTION("dissolution", "mekanism:dissolution", "kubejsstudio.mekanism.form.dissolution"),
    OXIDIZING("oxidizing", "mekanism:oxidizing", "kubejsstudio.mekanism.form.oxidizing"),
    PURIFYING("purifying", "mekanism:purifying", "kubejsstudio.mekanism.form.purifying"),
    INJECTING("injecting", "mekanism:injecting", "kubejsstudio.mekanism.form.injecting"),
    COMPRESSING("compressing", "mekanism:compressing", "kubejsstudio.mekanism.form.compressing"),
    ROTARY("rotary", "mekanism:rotary", "kubejsstudio.mekanism.form.rotary"),
    REACTION("reaction", "mekanism:reaction", "kubejsstudio.mekanism.form.reaction");

    private final String shortName;
    private final String typeId;
    private final String langKey;

    MekanismGasRecipeType(String shortName, String typeId, String langKey) {
        this.shortName = shortName;
        this.typeId = typeId;
        this.langKey = langKey;
    }

    public String shortName() { return shortName; }
    public String typeId() { return typeId; }
    public String langKey() { return langKey; }

    public static MekanismGasRecipeType byTypeId(String id) {
        for (MekanismGasRecipeType t : values()) {
            if (t.typeId.equals(id)) return t;
        }
        return null;
    }
}
