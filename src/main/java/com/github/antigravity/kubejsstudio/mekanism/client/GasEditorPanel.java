package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import dev.vfyjxf.taffy.style.FlexDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * Native LDLib2 UIElement panel for editing and creating Mekanism gases,
 * seamlessly matching KubeJS Studio's layout, theme, and styling.
 */
public class GasEditorPanel extends UIElement {
    private static final float LABEL_WIDTH = 130.0f;

    private final Label headerLabel;
    private final Label statusLabel;
    private final TextField idField;
    private final TextField displayNameField;
    private final TextField colorField;
    private final UIElement colorSwatch;
    private final TextField textureField;
    private final TextField radioactivityField;
    private final TextField tagsField;
    private final Button queueButton;

    public GasEditorPanel() {
        this.addClass("panel");
        this.layout(l -> {
            l.flexDirection(FlexDirection.COLUMN);
            l.paddingAll(14.0f);
            l.gapAll(10.0f);
            l.flexGrow(1.0f);
            l.widthPercent(100.0f);
            l.heightPercent(100.0f);
        });

        // 1. Header Section
        UIElement headerRow = new UIElement();
        headerRow.layout(l -> {
            l.flexDirection(FlexDirection.COLUMN);
            l.gapAll(2.0f);
        });

        this.headerLabel = new Label();
        this.headerLabel.setText("Mekanism Gas Builder / 新規ガス作成");
        this.headerLabel.layout(l -> l.flexGrow(1.0f));

        this.statusLabel = new Label();
        this.statusLabel.setText("Configure custom Mekanism gas properties below");
        this.statusLabel.addClass("muted");

        headerRow.addChildren(this.headerLabel, this.statusLabel);
        this.addChild(headerRow);

        // 2. Scrollable Rows Container
        ScrollerView scroller = new ScrollerView();
        scroller.layout(l -> {
            l.flexGrow(1.0f);
            l.widthPercent(100.0f);
            l.gapAll(8.0f);
        });

        // ID Row
        this.idField = new TextField();
        this.idField.setText("test_gas");
        this.idField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Gas ID / ガスID", this.idField));

        // Display Name Row
        this.displayNameField = new TextField();
        this.displayNameField.setText("Test Gas");
        this.displayNameField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Display Name / 表示名", this.displayNameField));

        // Color Row + Color Swatch Preview
        this.colorField = new TextField();
        this.colorField.setText("#00FFCC");
        this.colorField.layout(l -> l.flexGrow(1.0f));

        this.colorSwatch = new UIElement();
        this.colorSwatch.layout(l -> {
            l.width(20.0f);
            l.height(20.0f);
        });
        updateSwatch(0x00FFCC);

        this.colorField.setTextResponder(text -> {
            int parsed = parseColor(text);
            updateSwatch(parsed);
        });

        UIElement colorContainer = new UIElement();
        colorContainer.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(8.0f);
            l.flexGrow(1.0f);
        });
        colorContainer.addChildren(this.colorField, this.colorSwatch);
        scroller.addScrollViewChild(createFieldRow("Color / 色 (Hex)", colorContainer));

        // Texture Row
        this.textureField = new TextField();
        this.textureField.setText("mekanism:liquid/liquid");
        this.textureField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Texture / テクスチャ", this.textureField));

        // Radioactivity Row
        this.radioactivityField = new TextField();
        this.radioactivityField.setText("0.0");
        this.radioactivityField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Radioactivity / 放射能", this.radioactivityField));

        // Tags Row
        this.tagsField = new TextField();
        this.tagsField.setText("forge:gases");
        this.tagsField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Tags / タグ (カンマ区切り)", this.tagsField));

        this.addChild(scroller);

        // 3. Action Section (Queue button)
        UIElement actionRow = new UIElement();
        actionRow.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(8.0f);
        });

        this.queueButton = new Button();
        this.queueButton.setText("Queue Gas / 保留リストに追加");
        this.queueButton.addClass("primary-button");
        this.queueButton.layout(l -> l.flexGrow(1.0f));
        this.queueButton.setOnClick(e -> handleQueueGas());

        actionRow.addChild(this.queueButton);
        this.addChild(actionRow);
    }

    private UIElement createFieldRow(String labelText, UIElement fieldElement) {
        UIElement row = new UIElement();
        row.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(8.0f);
            l.widthPercent(100.0f);
        });

        Label label = new Label();
        label.setText(labelText);
        label.layout(l -> l.width(LABEL_WIDTH));

        row.addChildren(label, fieldElement);
        return row;
    }

    private void updateSwatch(int rgb) {
        int argb = 0xFF000000 | (rgb & 0xFFFFFF);
        this.colorSwatch.style(s -> s.background(new ColorRectTexture(argb)));
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

    private void handleQueueGas() {
        String id = this.idField.getValue() != null ? this.idField.getValue().trim() : "";
        if (id.isEmpty()) {
            this.statusLabel.setText("§cGas ID cannot be empty / ガスIDを入力してください");
            return;
        }

        String displayName = this.displayNameField.getValue() != null ? this.displayNameField.getValue().trim() : "";
        if (displayName.isEmpty()) {
            displayName = id;
        }

        int parsedColor = parseColor(this.colorField.getValue());
        double radioactivity = 0.0;
        try {
            if (this.radioactivityField.getValue() != null) {
                radioactivity = Double.parseDouble(this.radioactivityField.getValue().trim());
            }
        } catch (NumberFormatException ignored) {}

        String texture = this.textureField.getValue() != null ? this.textureField.getValue().trim() : "";
        if (texture.isEmpty()) {
            texture = "mekanism:liquid/liquid";
        }

        List<String> tags = new ArrayList<>();
        String rawTags = this.tagsField.getValue() != null ? this.tagsField.getValue().trim() : "";
        if (!rawTags.isEmpty()) {
            for (String t : rawTags.split(",")) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    tags.add(trimmed);
                }
            }
        }

        MekanismGasDefinition def = MekanismGasDefinition.builder(id)
                .displayName(displayName)
                .color(parsedColor)
                .radioactivity(radioactivity)
                .texture(texture)
                .tags(tags)
                .build();

        StudioWorkspaceHelper.queueGasIntoWorkspace(def);
        GasCreateUi.queuePendingGas(def);

        this.statusLabel.setText("§aQueued gas '" + id + "' into Workspace! / 保留リストに追加しました");
    }
}
