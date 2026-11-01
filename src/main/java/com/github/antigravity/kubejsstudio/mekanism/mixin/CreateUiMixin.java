package com.github.antigravity.kubejsstudio.mekanism.mixin;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.client.GasEditorPanel;
import com.github.antigravity.kubejsstudio.mekanism.config.MekanismCompatConfig;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SplitView;
import dev.tysontheember.kubejsstudio.client.ui.kit.BrowserList;
import dev.tysontheember.kubejsstudio.client.ui.kit.PendingDock;
import dev.tysontheember.kubejsstudio.client.ui.screen.CreateUi;
import dev.tysontheember.kubejsstudio.core.op.CreateEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Mixin for KubeJS Studio's CreateUi.
 * Seamlessly integrates the "Chemicals" mode button into the mode bar (Items, Blocks, Fluids, Chemicals)
 * with 100% synchronized state toggling, zero overlapping text/widgets, and smooth bidirectional switching.
 */
@Mixin(value = CreateUi.class, remap = false)
public abstract class CreateUiMixin {

    @Shadow
    private Map<CreateEntry.Kind, Button> modeBtns;

    @Shadow
    private CreateEntry.Kind mode;

    @Shadow
    private PendingDock dock;

    @Shadow
    private BrowserList list;

    @Shadow
    private Label headerLabel;

    @Shadow
    private Label statusLabel;

    @Shadow
    private ScrollerView rows;

    @Shadow
    private UIElement root;

    @Shadow
    private void syncToggle() {}

    @Shadow
    private void refreshList() {}

    @Shadow
    private void startNew() {}

    @Unique
    private Button chemicalBtn;

    @Unique
    private GasEditorPanel chemicalEditor;

    @Unique
    private UIElement studioEditorContainer;

    @Unique
    private boolean chemicalActive = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (!MekanismCompatConfig.ENABLE_GAS_BUILDER.get()) return;

        try {
            if (this.modeBtns == null || this.modeBtns.isEmpty()) return;
            Button firstBtn = this.modeBtns.values().iterator().next();
            UIElement modeBar = firstBtn.getParent();
            if (modeBar == null) return;

            // Direct resolution: this.rows is an immediate child of Studio's right editor pane container!
            if (this.rows != null && this.rows.getParent() != null) {
                this.studioEditorContainer = this.rows.getParent();
            } else {
                this.studioEditorContainer = findRightPaneInTree(this.root);
            }

            if (this.studioEditorContainer == null) {
                KubeJSStudioMekanismCompat.LOGGER.warn("Could not find right editor pane in CreateUi for GasEditorPanel injection.");
                return;
            }

            // Create and inject our native GasEditorPanel into the right editor pane
            this.chemicalEditor = new GasEditorPanel();
            this.chemicalEditor.addClass("gas-editor-panel");
            this.chemicalEditor.setDisplay(false);
            this.chemicalEditor.layout(l -> {
                l.flexGrow(1.0f);
                l.widthPercent(100.0f);
                l.heightPercent(100.0f);
            });

            // Wire Queue callback directly to Studio's PendingDock refresh
            this.chemicalEditor.setOnQueued(() -> {
                if (this.dock != null) {
                    this.dock.refresh();
                }
            });

            this.studioEditorContainer.addChild(this.chemicalEditor);

            // Create and inject "Chemicals" mode Button matching Studio styling
            this.chemicalBtn = new Button();
            this.chemicalBtn.setText("Chemicals");
            this.chemicalBtn.addClass("gas-mode-btn");
            this.chemicalBtn.layout(l -> l.flexGrow(1.0f));

            this.chemicalBtn.setOnClick(ev -> activateChemicalMode());

            modeBar.addChild(this.chemicalBtn);
            KubeJSStudioMekanismCompat.LOGGER.info("CreateUiMixin successfully injected Chemicals mode into CreateUi right editor pane!");

        } catch (Throwable t) {
            KubeJSStudioMekanismCompat.LOGGER.error("Error in CreateUiMixin initialization: {}", t.getMessage(), t);
        }
    }

    /**
     * Intercepts mode change requests (Items, Blocks, Fluids) and deactivates Chemicals mode cleanly.
     */
    @Inject(method = "setMode", at = @At("HEAD"))
    private void onSetMode(CreateEntry.Kind kind, CallbackInfo ci) {
        deactivateChemicalMode();
    }

    /**
     * Prevents Studio from overwriting the right pane with Fluid/Item/Block fields when in Chemical mode.
     */
    @Inject(method = "startNew", at = @At("HEAD"), cancellable = true)
    private void onStartNew(CallbackInfo ci) {
        if (this.chemicalActive) {
            if (this.chemicalEditor != null) {
                this.chemicalEditor.startNew();
            }
            ci.cancel();
        }
    }

    /**
     * Synchronizes toggle button highlight state across all buttons.
     */
    @Inject(method = "syncToggle", at = @At("RETURN"))
    private void onSyncToggle(CallbackInfo ci) {
        if (this.chemicalBtn != null) {
            if (this.chemicalActive) {
                this.chemicalBtn.addClass("seg-active");
                if (this.modeBtns != null) {
                    for (Button b : this.modeBtns.values()) {
                        b.removeClass("seg-active");
                    }
                }
            } else {
                this.chemicalBtn.removeClass("seg-active");
            }
        }
    }

    @Unique
    private void activateChemicalMode() {
        this.chemicalActive = true;
        // Crucial: reset this.mode so subsequent clicks on the previous mode (e.g. Fluids) will never be skipped by `if (this.mode == kind) return;`!
        this.mode = null;

        // Remove active state from Items, Blocks, Fluids buttons
        if (this.modeBtns != null) {
            for (Button b : this.modeBtns.values()) {
                b.removeClass("seg-active");
            }
        }
        if (this.chemicalBtn != null) {
            this.chemicalBtn.addClass("seg-active");
        }

        // Hide all original Studio editor widgets completely to eliminate any text/widget overlap
        if (this.studioEditorContainer != null) {
            for (UIElement child : this.studioEditorContainer.getChildren()) {
                if (child == this.chemicalEditor) {
                    child.setDisplay(true);
                } else {
                    child.setDisplay(false);
                }
            }
        }

        if (this.chemicalEditor != null) {
            this.chemicalEditor.startNew();
        }
    }

    @Unique
    private void deactivateChemicalMode() {
        if (!this.chemicalActive) return;
        this.chemicalActive = false;

        if (this.chemicalBtn != null) {
            this.chemicalBtn.removeClass("seg-active");
        }

        // Restore all original Studio editor widgets and hide GasEditorPanel
        if (this.studioEditorContainer != null) {
            for (UIElement child : this.studioEditorContainer.getChildren()) {
                if (child == this.chemicalEditor) {
                    child.setDisplay(false);
                } else {
                    child.setDisplay(true);
                }
            }
        }
    }

    @Unique
    private static UIElement findRightPaneInTree(UIElement el) {
        if (el == null) return null;
        if (el instanceof SplitView sv) {
            String className = sv.getClass().getSimpleName();
            if (className.contains("HSplit")) {
                return sv.second;
            }
        }
        for (UIElement child : el.getChildren()) {
            UIElement found = findRightPaneInTree(child);
            if (found != null) return found;
        }
        return null;
    }
}
