package com.github.antigravity.kubejsstudio.mekanism.recipe;

import mekanism.api.MekanismAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

/**
 * Safe lookup utility to check if an ID or tag belongs to Mekanism's chemical registries
 * (Gas, Slurry, InfuseType, Pigment) or recognized addon chemicals.
 */
public final class MekanismRegistryLookup {
    private MekanismRegistryLookup() {}

    /**
     * Checks if the given string represents a valid registered chemical or known gas ID.
     */
    public static boolean isValidChemicalId(String id) {
        if (id == null || id.isBlank()) return false;
        String clean = id.trim();
        if (clean.startsWith("#")) {
            return isValidChemicalTag(clean);
        }

        ResourceLocation loc = ResourceLocation.tryParse(clean);
        if (loc == null) return false;

        try {
            if (ModList.get() != null && ModList.get().isLoaded("mekanism")) {
                if (MekanismAPI.gasRegistry() != null && MekanismAPI.gasRegistry().containsKey(loc)) return true;
                if (MekanismAPI.slurryRegistry() != null && MekanismAPI.slurryRegistry().containsKey(loc)) return true;
                if (MekanismAPI.infuseTypeRegistry() != null && MekanismAPI.infuseTypeRegistry().containsKey(loc)) return true;
                if (MekanismAPI.pigmentRegistry() != null && MekanismAPI.pigmentRegistry().containsKey(loc)) return true;
            }
        } catch (Throwable ignored) {}

        // Addon or synthetic fallback check
        String ns = loc.getNamespace();
        if ("mekanism".equals(ns) || "astral_mekanism".equals(ns) || "mekanism_extras".equals(ns) || "evolvedmekanism".equals(ns)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the given string represents a valid gas or chemical tag.
     */
    public static boolean isValidChemicalTag(String tag) {
        if (tag == null || tag.isBlank()) return false;
        String clean = tag.trim();
        if (clean.startsWith("#")) {
            clean = clean.substring(1);
        }

        ResourceLocation loc = ResourceLocation.tryParse(clean);
        if (loc == null) return false;

        String path = loc.getPath();
        if (path.startsWith("gases/") || path.startsWith("gas/") || path.startsWith("slurry/") || path.startsWith("infuse_type/")) {
            return true;
        }

        String ns = loc.getNamespace();
        if ("mekanism".equals(ns) || "astral_mekanism".equals(ns) || "forge".equals(ns)) {
            return true;
        }

        return false;
    }
}
