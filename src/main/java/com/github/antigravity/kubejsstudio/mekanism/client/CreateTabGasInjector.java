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
        Screen screen = event.getScreen();
        if (!(screen instanceof ModularUIScreen modularScreen)) return;
        if (!MekanismCompatConfig.ENABLE_GAS_BUILDER.get()) return;

        try {
            UIElement root = modularScreen.getModularUI().ui.rootElement;
            if (root == null) return;

            // Search for the segmented mode button bar containing "Items", "Blocks", "Fluids"
            Button itemBtn = findButtonByText(root, "Items");
            if (itemBtn == null) {
                // Not in Create tab or CreateUi is not active
                return;
            }

            UIElement modeBar = itemBtn.getParent();
            if (modeBar == null) return;

            // Check if already injected on this modeBar
            for (UIElement child : modeBar.getChildren()) {
                if (child.hasClass("gas-mode-btn")) {
                    return;
                }
            }

            // Find the horizontal SplitView separating the browser (first) and editor (second)
            SplitView hSplit = findSplitView(modeBar);
            if (hSplit == null) {
                hSplit = findSplitViewInTree(root);
            }
            if (hSplit == null) {
                KubeJSStudioMekanismCompat.LOGGER.warn("Could not find horizontal SplitView in CreateUi. Aborting injection.");
                return;
            }

            UIElement rightEditorContainer = hSplit.second;
            if (rightEditorContainer == null) {
                KubeJSStudioMekanismCompat.LOGGER.warn("SplitView.second is null. Aborting injection.");
                return;
            }

            // The original Studio editor pane is the child of SplitView.second
            final UIElement studioEditor = rightEditorContainer.getChildren().isEmpty() ? null : rightEditorContainer.getChildren().get(0);

            // Create our native GasEditorPanel and add it to the RIGHT pane (never to left browser!)
            final GasEditorPanel gasEditor = new GasEditorPanel();
            gasEditor.addClass("gas-editor-panel");
            gasEditor.setDisplay(false); // Initially hidden
            gasEditor.layout(l -> {
                l.flexGrow(1.0f);
                l.widthPercent(100.0f);
                l.heightPercent(100.0f);
            });
            rightEditorContainer.addChild(gasEditor);

            // Create our native LDLib2 "Chemicals" toggle button
            final Button gasBtn = new Button();
            gasBtn.setText("Chemicals");
            gasBtn.addClass("gas-mode-btn");
            gasBtn.layout(l -> l.flexGrow(1.0f));

            // Helper to activate Chemicals / Gas mode
            Runnable showGasMode = () -> {
                for (UIElement child : modeBar.getChildren()) {
                    if (child instanceof Button b && b != gasBtn) {
                        b.removeClass("seg-active");
                    }
                }
                gasBtn.addClass("seg-active");

                if (studioEditor != null) {
                    studioEditor.setDisplay(false);
                }
                gasEditor.setDisplay(true);
            };

            // Helper to restore standard Studio editor mode (Items, Blocks, Fluids)
            Runnable restoreStudioMode = () -> {
                gasBtn.removeClass("seg-active");
                gasEditor.setDisplay(false);
                if (studioEditor != null) {
                    studioEditor.setDisplay(true);
                }
            };

            // Clicking Chemicals button switches to Gas editor
            gasBtn.setOnClick(e -> showGasMode.run());

            // Capture clicks on the modeBar: if any sibling button is clicked, restore standard editor
            modeBar.addEventListener("click", ev -> {
                UIElement target = ev.target;
                boolean isGasClick = (target == gasBtn || (target != null && target.getParent() == gasBtn));
                if (!isGasClick) {
                    restoreStudioMode.run();
                }
            }, true);

            // Also attach capture listeners directly to each sibling button
            for (UIElement child : modeBar.getChildren()) {
                if (child instanceof Button b && b != gasBtn) {
                    b.addEventListener("click", ev -> restoreStudioMode.run(), true);
                }
            }

            // Finally, inject the Chemicals button into the mode bar beside Items, Blocks, Fluids
            modeBar.addChild(gasBtn);
            KubeJSStudioMekanismCompat.LOGGER.info("Successfully injected native LDLib2 [Chemicals] button into CreateUi right editor pane!");

        } catch (Throwable t) {
            KubeJSStudioMekanismCompat.LOGGER.error("Failed to inject Chemicals tab into KubeJS Studio UI: {}", t.getMessage(), t);
        }
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
