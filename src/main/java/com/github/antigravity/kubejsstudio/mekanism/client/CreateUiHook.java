package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.config.MekanismCompatConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles injection of the "Gases" mode into KubeJS Studio's Create Tab
 * and registers helper commands for direct access to the Gas builder.
 */
public final class CreateUiHook {
    private static final GasCreateUi GAS_CREATE_UI = new GasCreateUi();

    private CreateUiHook() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(CreateUiHook.class);
        KubeJSStudioMekanismCompat.LOGGER.info("CreateUiHook initialized for Mekanism Gas visual creation.");
    }

    public static GasCreateUi getGasCreateUi() {
        return GAS_CREATE_UI;
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("kubejs_gas")
                        .then(Commands.literal("create")
                                .executes(ctx -> {
                                    openGasCreateForm(ctx.getSource());
                                    return 1;
                                }))
                        .then(Commands.literal("apply")
                                .executes(ctx -> {
                                    boolean ok = GAS_CREATE_UI.applyPendingToDisk();
                                    if (ok) {
                                        ctx.getSource().sendSuccess(() -> Component.translatable("kubejsstudio.mekanism.create.apply_success"), false);
                                    } else {
                                        ctx.getSource().sendFailure(Component.translatable("kubejsstudio.mekanism.create.apply_failed"));
                                    }
                                    return ok ? 1 : 0;
                                }))
                        .executes(ctx -> {
                            openGasCreateForm(ctx.getSource());
                            return 1;
                        })
        );
    }

    private static void openGasCreateForm(CommandSourceStack source) {
        if (!MekanismCompatConfig.ENABLE_GAS_BUILDER.get()) {
            source.sendFailure(Component.translatable("kubejsstudio.mekanism.create.disabled"));
            return;
        }

        source.sendSuccess(() -> Component.translatable("kubejsstudio.mekanism.create.open_notice"), false);
        // Note: The visual panel is automatically active inside Studio's Create tab.
    }
}
