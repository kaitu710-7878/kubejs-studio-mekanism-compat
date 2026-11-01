package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.config.MekanismCompatConfig;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SplitView;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Native LDLib2 injector for KubeJS Studio's Create Tab.
 * Integrates a matching "Chemicals" segment toggle button into the mode button bar
 * (Items, Blocks, Fluids, Chemicals) and switches the right editor pane to GasEditorPanel
 * without touching or corrupting the left browser pane.
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
        // Injection is now natively handled with 100% synchronization by CreateUiMixin.
        // This avoids any dual-event collision or race condition.
    }

    private static Button findButtonByText(UIElement element, String text) {
        if (element instanceof Button btn) {
            if (btn.text != null && text.equalsIgnoreCase(btn.text.getText().getString())) {
                return btn;
            }
        }
        for (UIElement child : element.getChildren()) {
            Button found = findButtonByText(child, text);
            if (found != null) return found;
        }
        return null;
    }

    private static SplitView findSplitView(UIElement start) {
        UIElement curr = start;
        while (curr != null) {
            if (curr instanceof SplitView sv) {
                return sv;
            }
            curr = curr.getParent();
        }
        return null;
    }

    private static SplitView findSplitViewInTree(UIElement root) {
        if (root instanceof SplitView sv) {
            return sv;
        }
        for (UIElement child : root.getChildren()) {
            SplitView found = findSplitViewInTree(child);
            if (found != null) return found;
        }
        return null;
    }
}
