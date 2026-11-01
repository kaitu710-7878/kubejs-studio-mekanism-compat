package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.config.MekanismCompatConfig;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Native LDLib2 injector for KubeJS Studio's Create Tab.
 * Seamlessly injects a matching "Gases" segment toggle button into the mode button bar
 * (Items, Blocks, Fluids, Gases) and switches the editor pane to GasEditorPanel when active.
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

            // Check if already injected
            for (UIElement child : modeBar.getChildren()) {
                if (child.hasClass("gas-mode-btn")) {
                    return; // Already injected on this bar
                }
            }

            // Find the right editor pane (sibling of the browser or within root)
            UIElement editorContainer = findEditorContainer(modeBar);

            // Create our native LDLib2 "Gases" toggle button
            Button gasBtn = new Button();
            gasBtn.setText("Gases");
            gasBtn.addClass("gas-mode-btn");
            gasBtn.layout(l -> l.flexGrow(1.0f));

            // Gas editor panel instance
            GasEditorPanel gasEditor = new GasEditorPanel();
            gasEditor.addClass("gas-editor-panel");

            // On click listener for Gases mode
            gasBtn.setOnClick(e -> {
                // Clear seg-active from siblings and activate Gases
                for (UIElement child : modeBar.getChildren()) {
                    child.removeClass("seg-active");
                }
                gasBtn.addClass("seg-active");

                // Switch editor pane to GasEditorPanel
                if (editorContainer != null) {
                    showGasEditor(editorContainer, gasEditor);
                }
            });

            // Wrap sibling buttons (Items, Blocks, Fluids) so clicking them restores default editor
            hookSiblingButtons(modeBar, gasBtn, editorContainer, gasEditor);

            // Inject the Gases button into the bar right beside Items, Blocks, Fluids
            modeBar.addChild(gasBtn);
            KubeJSStudioMekanismCompat.LOGGER.info("Successfully injected native LDLib2 [Gases] button into CreateUi mode bar!");

        } catch (Throwable t) {
            KubeJSStudioMekanismCompat.LOGGER.error("Failed to inject Gas tab into KubeJS Studio UI: {}", t.getMessage(), t);
        }
    }

    private static void showGasEditor(UIElement editorContainer, GasEditorPanel gasEditor) {
        // Hide existing editor children and show GasEditorPanel
        for (UIElement child : editorContainer.getChildren()) {
            if (child != gasEditor) {
                child.addClass("gas-hidden");
                child.layout(l -> l.width(0.0f).height(0.0f));
            }
        }
        if (!editorContainer.getChildren().contains(gasEditor)) {
            editorContainer.addChild(gasEditor);
        }
        gasEditor.layout(l -> {
            l.flexGrow(1.0f);
            l.widthPercent(100.0f);
            l.heightPercent(100.0f);
        });
    }

    private static void restoreOriginalEditor(UIElement editorContainer, GasEditorPanel gasEditor) {
        if (editorContainer != null) {
            for (UIElement child : editorContainer.getChildren()) {
                if (child != gasEditor) {
                    child.removeClass("gas-hidden");
                    child.layout(l -> {
                        l.flexGrow(1.0f);
                        l.widthPercent(100.0f);
                        l.heightPercent(100.0f);
                    });
                }
            }
            if (editorContainer.getChildren().contains(gasEditor)) {
                gasEditor.layout(l -> l.width(0.0f).height(0.0f));
            }
        }
    }

    private static void hookSiblingButtons(UIElement modeBar, Button gasBtn, UIElement editorContainer, GasEditorPanel gasEditor) {
        for (UIElement child : modeBar.getChildren()) {
            if (child instanceof Button btn && !btn.hasClass("gas-mode-btn")) {
                // Hook click to reset Gases active state
                btn.addEventListener("click", ev -> {
                    gasBtn.removeClass("seg-active");
                    restoreOriginalEditor(editorContainer, gasEditor);
                }, false);
            }
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

    private static UIElement findEditorContainer(UIElement modeBar) {
        UIElement curr = modeBar;
        while (curr != null && curr.getParent() != null) {
            UIElement parent = curr.getParent();
            List<UIElement> siblings = parent.getChildren();
            if (siblings.size() >= 2) {
                // In CollapsibleSplit or horizontal container, the other child is the editor pane
                for (UIElement sibling : siblings) {
                    if (sibling != curr && !sibling.getChildren().contains(modeBar)) {
                        return sibling;
                    }
                }
            }
            curr = parent;
        }
        return null;
    }
}
