package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Visual GUI screen for creating new Mekanism gases directly within KubeJS Studio.
 */
public class GasCreateScreen extends Screen {
    private final Screen parentScreen;

    private EditBox idBox;
    private EditBox nameBox;
    private EditBox colorBox;
    private EditBox textureBox;
    private EditBox radioactivityBox;
    private EditBox tagsBox;

    private Component statusMessage = Component.empty();
    private int statusColor = 0xFFFFFF;

    public GasCreateScreen(Screen parentScreen) {
        super(Component.translatable("gui.kubejs_studio_mekanism_compat.gas_builder.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 38;
        int boxW = 180;
        int boxH = 20;
        int spacing = 26;

        // 1. Gas ID
        this.idBox = new EditBox(this.font, centerX - 90, startY, boxW, boxH, Component.literal("ID"));
        this.idBox.setValue("test_gas");
        this.addRenderableWidget(this.idBox);

        // 2. Display Name
        this.nameBox = new EditBox(this.font, centerX - 90, startY + spacing, boxW, boxH, Component.literal("Display Name"));
        this.nameBox.setValue("Test Gas");
        this.addRenderableWidget(this.nameBox);

        // 3. Color Hex
        this.colorBox = new EditBox(this.font, centerX - 90, startY + spacing * 2, boxW, boxH, Component.literal("Color Hex"));
        this.colorBox.setValue("#00FFCC");
        this.addRenderableWidget(this.colorBox);

        // 4. Texture
        this.textureBox = new EditBox(this.font, centerX - 90, startY + spacing * 3, boxW, boxH, Component.literal("Texture"));
        this.textureBox.setValue("mekanism:liquid/liquid");
        this.addRenderableWidget(this.textureBox);

        // 5. Radioactivity
        this.radioactivityBox = new EditBox(this.font, centerX - 90, startY + spacing * 4, boxW, boxH, Component.literal("Radioactivity"));
        this.radioactivityBox.setValue("0.0");
        this.addRenderableWidget(this.radioactivityBox);

        // 6. Tags
        this.tagsBox = new EditBox(this.font, centerX - 90, startY + spacing * 5, boxW, boxH, Component.literal("Tags"));
        this.tagsBox.setValue("forge:gases");
        this.addRenderableWidget(this.tagsBox);

        // Action Buttons
        int btnY = startY + spacing * 6 + 8;
        Button queueBtn = Button.builder(
                Component.translatable("gui.kubejs_studio_mekanism_compat.gas_builder.queue"),
                b -> handleQueueGas()
        ).bounds(centerX - 95, btnY, 90, 20).build();

        Button cancelBtn = Button.builder(
                Component.translatable("gui.kubejs_studio_mekanism_compat.gas_builder.cancel"),
                b -> onClose()
        ).bounds(centerX + 5, btnY, 90, 20).build();

        this.addRenderableWidget(queueBtn);
        this.addRenderableWidget(cancelBtn);
    }

    private void handleQueueGas() {
        String id = this.idBox.getValue().trim();
        if (id.isEmpty()) {
            this.statusMessage = Component.literal("ID cannot be empty / IDを入力してください");
            this.statusColor = 0xFFFF5555;
            return;
        }

        String displayName = this.nameBox.getValue().trim();
        if (displayName.isEmpty()) {
            displayName = id;
        }

        int parsedColor = parseColor(this.colorBox.getValue());
        double radioactivity = 0.0;
        try {
            radioactivity = Double.parseDouble(this.radioactivityBox.getValue().trim());
        } catch (NumberFormatException ignored) {}

        String texture = this.textureBox.getValue().trim();
        if (texture.isEmpty()) {
            texture = "mekanism:liquid/liquid";
        }

        List<String> tagList = new ArrayList<>();
        String tagsRaw = this.tagsBox.getValue().trim();
        if (!tagsRaw.isEmpty()) {
            for (String t : tagsRaw.split(",")) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    tagList.add(trimmed);
                }
            }
        }

        MekanismGasDefinition def = MekanismGasDefinition.builder(id)
                .displayName(displayName)
                .color(parsedColor)
                .radioactivity(radioactivity)
                .texture(texture)
                .tags(tagList)
                .build();

        // 1. Studio Workspace
        boolean queued = StudioWorkspaceHelper.queueGasIntoWorkspace(def);
        // 2. Fallback / UI queue
        GasCreateUi.queuePendingGas(def);

        this.statusMessage = Component.literal("Queued gas '" + id + "'! / ガス '" + id + "' を保留に追加しました");
        this.statusColor = 0xFF55FF55;
    }

    private int parseColor(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0x00FFCC;
        }
        String clean = input.trim().replaceAll("^[#]|^(0x)", "");
        try {
            return (int) Long.parseLong(clean, 16);
        } catch (Exception e) {
            return 0x00FFCC;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        // Header Title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0x55FFFF);

        int centerX = this.width / 2;
        int startY = 38;
        int spacing = 26;

        // Labels
        guiGraphics.drawString(this.font, "Gas ID / ID:", centerX - 195, startY + 6, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Display Name / 表示名:", centerX - 215, startY + spacing + 6, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Color / 色Hex:", centerX - 195, startY + spacing * 2 + 6, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Texture / テクスチャ:", centerX - 215, startY + spacing * 3 + 6, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Radioactivity / 放射能:", centerX - 225, startY + spacing * 4 + 6, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Tags (comma-sep) / タグ:", centerX - 235, startY + spacing * 5 + 6, 0xAAAAAA);

        // Color Preview Swatch
        int previewX = centerX + 96;
        int previewY = startY + spacing * 2;
        int colorRgb = parseColor(this.colorBox.getValue());
        int argb = 0xFF000000 | (colorRgb & 0xFFFFFF);
        guiGraphics.fill(previewX, previewY, previewX + 20, previewY + 20, 0xFFFFFFFF);
        guiGraphics.fill(previewX + 1, previewY + 1, previewX + 19, previewY + 19, argb);

        // Status Message
        if (!this.statusMessage.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.statusMessage, centerX, startY + spacing * 6 + 32, this.statusColor);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }
}
