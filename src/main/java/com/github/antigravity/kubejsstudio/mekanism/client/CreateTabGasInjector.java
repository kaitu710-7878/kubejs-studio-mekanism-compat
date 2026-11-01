package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.config.MekanismCompatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Screen event listener that injects the visible "Gases" mode toggle button
 * onto KubeJS Studio screens using native Minecraft widgets.
 */
public final class CreateTabGasInjector {
    private static boolean registered = false;

    private CreateTabGasInjector() {}

    public static void register() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(CreateTabGasInjector.class);
            registered = true;
            KubeJSStudioMekanismCompat.LOGGER.info("CreateTabGasInjector registered on MinecraftForge.EVENT_BUS.");
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen == null || !MekanismCompatConfig.ENABLE_GAS_BUILDER.get()) return;

        String className = screen.getClass().getName();
        // Check if the current screen is KubeJS Studio's ModularUIScreen or StudioScreen
        if (className.contains("kubejsstudio") || className.contains("ModularUI") || isStudioScreen(screen)) {
            // Position the "Gases" toggle button visibly on the upper toolbar / header area
            int btnW = 70;
            int btnH = 20;
            int btnX = 175; // Right beside the Create tab area on standard layouts
            int btnY = 5;   // Header bar

            Button gasToggleBtn = Button.builder(
                    Component.literal("§b[ Gases ]"),
                    btn -> {
                        Minecraft.getInstance().setScreen(new GasCreateScreen(screen));
                    }
            )
            .bounds(btnX, btnY, btnW, btnH)
            .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                    Component.literal("Mekanism Gas Builder / 新規ガス作成フォームを開く")
            ))
            .build();

            event.addListener(gasToggleBtn);
            KubeJSStudioMekanismCompat.LOGGER.info("Injected visible 'Gases' mode button at ({}, {}) on screen: {}", btnX, btnY, className);
        }
    }

    private static boolean isStudioScreen(Screen screen) {
        try {
            Class<?> studioUiCls = Class.forName("dev.tysontheember.kubejsstudio.client.ui.StudioUi");
            java.lang.reflect.Method isStudioMethod = studioUiCls.getMethod("isStudioScreen");
            return Boolean.TRUE.equals(isStudioMethod.invoke(null));
        } catch (Throwable ignored) {
            return false;
        }
    }
}
