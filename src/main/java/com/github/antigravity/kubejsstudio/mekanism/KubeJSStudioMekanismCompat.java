package com.github.antigravity.kubejsstudio.mekanism;

import com.github.antigravity.kubejsstudio.mekanism.client.CreateTabGasInjector;
import com.github.antigravity.kubejsstudio.mekanism.client.CreateUiHook;
import com.github.antigravity.kubejsstudio.mekanism.config.MekanismCompatConfig;
import com.github.antigravity.kubejsstudio.mekanism.integration.KubeJsStudioBridge;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for KubeJS Studio Mekanism Compat.
 * Provides deep integration for Mekanism Gas creation, Tag management, and Recipe forms in KubeJS Studio.
 */
@Mod(KubeJSStudioMekanismCompat.MODID)
public class KubeJSStudioMekanismCompat {
    public static final String MODID = "kubejs_studio_mekanism_compat";
    public static final String MOD_NAME = "KubeJS Studio Mekanism Compat";
    public static final String VERSION = "1.0.0-alpha.11";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public KubeJSStudioMekanismCompat() {
        LOGGER.info("Initializing {} v{}", MOD_NAME, VERSION);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MekanismCompatConfig.SPEC, "kubejs-studio-mekanism-compat.toml");

        // Initialize core registries
        MekanismGasRecipeRegistry.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[{}] Common setup initialized.", MOD_NAME);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("[{}] Client setup initialized. Registering Studio UI hooks...", MOD_NAME);
        event.enqueueWork(() -> {
            CreateTabGasInjector.register();
            CreateUiHook.init();
            KubeJsStudioBridge.attemptHook();
        });
    }
}
