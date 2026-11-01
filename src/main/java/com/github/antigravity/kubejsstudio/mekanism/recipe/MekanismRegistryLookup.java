package com.github.antigravity.kubejsstudio.mekanism.recipe;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

/**
 * Safe lookup utility to check if an ID or tag belongs to Mekanism's chemical registries
 * (Gas, Slurry, InfuseType, Pigment) or recognized addon chemicals.
 */
public final class MekanismRegistryLookup {
    private MekanismRegistryLookup() {}

    /**
     * Retrieves the Chemical object for a given ID, or null if not registered.
     */
    public static Chemical<?> getChemical(String id) {
        if (id == null || id.isBlank()) return null;
        String clean = id.trim();
        if (clean.startsWith("#")) return null;
        ResourceLocation loc = ResourceLocation.tryParse(clean);
        if (loc == null) return null;

        try {
            if (ModList.get() != null && ModList.get().isLoaded("mekanism")) {
                if (MekanismAPI.gasRegistry() != null && MekanismAPI.gasRegistry().containsKey(loc)) {
                    return MekanismAPI.gasRegistry().getValue(loc);
                }
                if (MekanismAPI.slurryRegistry() != null && MekanismAPI.slurryRegistry().containsKey(loc)) {
                    return MekanismAPI.slurryRegistry().getValue(loc);
                }
                if (MekanismAPI.infuseTypeRegistry() != null && MekanismAPI.infuseTypeRegistry().containsKey(loc)) {
                    return MekanismAPI.infuseTypeRegistry().getValue(loc);
                }
                if (MekanismAPI.pigmentRegistry() != null && MekanismAPI.pigmentRegistry().containsKey(loc)) {
                    return MekanismAPI.pigmentRegistry().getValue(loc);
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Gets the RGB tint color of the chemical, or generates a deterministic fallback color.
     */
    public static int getChemicalColor(String id) {
        Chemical<?> chem = getChemical(id);
        if (chem != null) {
            int tint = chem.getTint();
            if (tint != 0) return tint;
            int rep = chem.getColorRepresentation();
            if (rep != 0) return rep;
        }
        return fallbackColor(id);
    }

    private static int fallbackColor(String id) {
        if (id == null) return 0x33FF99;
        int h = Math.abs(id.hashCode());
        int r = (h & 0xFF0000) >> 16;
        int g = (h & 0x00FF00) >> 8;
        int b = (h & 0x0000FF);
        // Ensure not too dark
        r = Math.max(60, r);
        g = Math.max(60, g);
        b = Math.max(60, b);
        return (r << 16) | (g << 8) | b;
    }

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
