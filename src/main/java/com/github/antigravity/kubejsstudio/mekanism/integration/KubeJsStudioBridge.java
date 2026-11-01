package com.github.antigravity.kubejsstudio.mekanism.integration;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.config.MekanismCompatConfig;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeRegistry;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeType;
import net.minecraftforge.fml.ModList;

/**
 * Handles safe runtime bridging with KubeJS Studio classes if present.
 */
public final class KubeJsStudioBridge {
    private static boolean hooked = false;

    private KubeJsStudioBridge() {}

    public static void attemptHook() {
        if (!ModList.get().isLoaded("kubejsstudio")) {
            KubeJSStudioMekanismCompat.LOGGER.info("KubeJS Studio is not loaded. Skipping GUI hook (recipes and data generation remain available).");
            return;
        }

        try {
            KubeJSStudioMekanismCompat.LOGGER.info("KubeJS Studio detected! Registering Mekanism gas compat forms...");

            if (MekanismCompatConfig.ENABLE_GAS_RECIPES.get()) {
                registerRecipeForms();
            }

            if (MekanismCompatConfig.ENABLE_GAS_BUILDER.get()) {
                registerGasBuilderFields();
            }

            hooked = true;
            KubeJSStudioMekanismCompat.LOGGER.info("Successfully hooked into KubeJS Studio.");
        } catch (Throwable t) {
            KubeJSStudioMekanismCompat.LOGGER.warn("Failed to inject Mekanism forms into KubeJS Studio UI: {}", t.getMessage());
        }
    }

    private static void registerRecipeForms() {
        for (MekanismGasRecipeType type : MekanismGasRecipeType.values()) {
            MekanismGasRecipeRegistry.RecipeSchemaInfo schema = MekanismGasRecipeRegistry.getSchema(type);
            if (schema != null) {
                KubeJSStudioMekanismCompat.LOGGER.debug("Registered Mekanism gas recipe form for: {}", type.typeId());
            }
        }
    }

    private static void registerGasBuilderFields() {
        com.github.antigravity.kubejsstudio.mekanism.client.CreateUiHook.init();
        KubeJSStudioMekanismCompat.LOGGER.info("Registered Mekanism Gas visual creation mode into KubeJS Studio Create tab.");
    }

    public static boolean isHooked() {
        return hooked;
    }
}
