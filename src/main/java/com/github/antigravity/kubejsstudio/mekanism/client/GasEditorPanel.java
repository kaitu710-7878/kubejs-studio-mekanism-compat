package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * Native LDLib2 UIElement panel for editing and creating Mekanism chemicals (Gases, Infuse Types, Pigments, Slurries),
 * seamlessly matching KubeJS Studio's right-pane editor layout, styling, and classes with ZERO text overlap.
 */
public class GasEditorPanel extends UIElement {
    private static final float LABEL_WIDTH = 110.0f;

    private final TextField nsField;
    private final TextField pathField;
    private final Selector<String> typeSelector;
    private final TextField displayNameField;
    private final TextField colorField;
    private final UIElement colorSwatch;
    private final TextField textureField;
    private final TextField radioactivityField;
    private final TextField tagsField;
    private final Label statusLabel;
    private final Label headerTitleLabel;
    private final Label headerSubLabel;

    private Runnable onQueuedCallback;

    public GasEditorPanel() {
        this.layout(l -> {
            l.flexDirection(FlexDirection.COLUMN);
            l.gapAll(8.0f);
            l.flexGrow(1.0f);
            l.widthPercent(100.0f);
            l.heightPercent(100.0f);
        });

        // 1. Header Row: "New chemical  Give it an id, then queue it" ... "Output: KubeJS only"
        UIElement headerRow = new UIElement();
        headerRow.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.alignItems(AlignItems.BASELINE);
            l.justifyContent(AlignContent.SPACE_BETWEEN);
            l.widthPercent(100.0f);
        });

        UIElement headerLeft = new UIElement();
        headerLeft.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(8.0f);
            l.alignItems(AlignItems.BASELINE);
        });

        this.headerTitleLabel = new Label();
        this.headerTitleLabel.setText("New chemical");

        this.headerSubLabel = new Label();
        this.headerSubLabel.setText("Give it an id, then queue it");
        this.headerSubLabel.addClass("muted");

        headerLeft.addChildren(this.headerTitleLabel, this.headerSubLabel);

        Label outputLabel = new Label();
        outputLabel.setText("Output: KubeJS only");
        outputLabel.addClass("muted");

        headerRow.addChildren(headerLeft, outputLabel);
        this.addChild(headerRow);

        // Divider
        this.addChild(createDivider());

        // 2. ID Row: Id [kubejs] : [path]
        UIElement idRow = new UIElement();
        idRow.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(6.0f);
            l.alignItems(AlignItems.CENTER);
            l.widthPercent(100.0f);
        });

        Label idLabel = createFieldLabel("Id");
        this.nsField = new TextField();
        this.nsField.setText("kubejs");
        this.nsField.layout(l -> l.width(80.0f));

        Label colon = new Label();
        colon.setText(":");
        colon.layout(l -> l.width(8.0f));

        this.pathField = new TextField();
        this.pathField.setText("");
        this.pathField.layout(l -> l.flexGrow(1.0f));

        idRow.addChildren(idLabel, this.nsField, colon, this.pathField);
        this.addChild(idRow);

        // 3. Type Row: Type [ gas v ]
        UIElement typeRow = new UIElement();
        typeRow.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(6.0f);
            l.alignItems(AlignItems.CENTER);
            l.widthPercent(100.0f);
        });

        Label typeLabel = createFieldLabel("Type");
        this.typeSelector = new Selector<>();
        this.typeSelector.setCandidates(List.of("gas", "infuse_type", "pigment", "slurry"));
        this.typeSelector.setValue("gas", false);
        this.typeSelector.layout(l -> l.flexGrow(1.0f));

        typeRow.addChildren(typeLabel, this.typeSelector);
        this.addChild(typeRow);

        // 4. Scrollable Properties Area (ScrollerView with list-inset)
        ScrollerView scroller = new ScrollerView();
        scroller.addClass("list-inset");
        scroller.layout(l -> {
            l.flexGrow(1.0f);
            l.widthPercent(100.0f);
            l.gapAll(6.0f);
            l.paddingAll(6.0f);
        });

        // Display Name Row
        this.displayNameField = new TextField();
        this.displayNameField.setText("");
        this.displayNameField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Display name", this.displayNameField));

        // Colour Row + Swatch
        this.colorField = new TextField();
        this.colorField.setText("#00FFCC");
        this.colorField.layout(l -> l.flexGrow(1.0f));

        this.colorSwatch = new UIElement();
        this.colorSwatch.layout(l -> {
            l.width(18.0f);
            l.height(18.0f);
        });
        updateSwatch(0x00FFCC);

        this.colorField.addEventListener("change", ev -> {
            int parsed = parseColor(this.colorField.getValue());
            updateSwatch(parsed);
        });

        UIElement colorContainer = new UIElement();
        colorContainer.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(6.0f);
            l.alignItems(AlignItems.CENTER);
            l.flexGrow(1.0f);
        });
        colorContainer.addChildren(this.colorField, this.colorSwatch);
        scroller.addScrollViewChild(createFieldRow("Tint colour", colorContainer));

        // Texture Row
        this.textureField = new TextField();
        this.textureField.setText("mekanism:liquid/liquid");
        this.textureField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Texture", this.textureField));

        // Radioactivity Row
        this.radioactivityField = new TextField();
        this.radioactivityField.setText("0.0");
        this.radioactivityField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Radioactivity", this.radioactivityField));

        // Tags Row
        this.tagsField = new TextField();
        this.tagsField.setText("forge:gases");
        this.tagsField.layout(l -> l.flexGrow(1.0f));
        scroller.addScrollViewChild(createFieldRow("Tags", this.tagsField));

        this.addChild(scroller);

        // Divider
        this.addChild(createDivider());

        // 5. Bottom Action Bar: Revert, Queue
        UIElement bottomRow = new UIElement();
        bottomRow.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.justifyContent(AlignContent.FLEX_END);
            l.alignItems(AlignItems.CENTER);
            l.gapAll(8.0f);
            l.widthPercent(100.0f);
        });

        this.statusLabel = new Label();
        this.statusLabel.setText("");
        this.statusLabel.addClass("muted");
        this.statusLabel.layout(l -> l.flexGrow(1.0f));

        Button revertBtn = new Button();
        revertBtn.setText("Revert");
        revertBtn.layout(l -> l.width(60.0f));
        revertBtn.setOnClick(e -> handleRevert());

        Button queueBtn = new Button();
        queueBtn.setText("Queue");
        queueBtn.addClass("primary-button");
        queueBtn.layout(l -> l.width(60.0f));
        queueBtn.setOnClick(e -> handleQueue());

        bottomRow.addChildren(this.statusLabel, revertBtn, queueBtn);
        this.addChild(bottomRow);
    }

    public void setOnQueued(Runnable callback) {
        this.onQueuedCallback = callback;
    }

    public void startNew() {
        this.headerTitleLabel.setText("New chemical");
        this.headerSubLabel.setText("Give it an id, then queue it");
        this.nsField.setText("kubejs");
        this.pathField.setText("");
        this.displayNameField.setText("");
        this.colorField.setText("#00FFCC");
        updateSwatch(0x00FFCC);
        this.textureField.setText("mekanism:liquid/liquid");
        this.radioactivityField.setText("0.0");
        this.tagsField.setText("forge:gases");
        this.statusLabel.setText("");
    }

    public void selectGas(String fullId) {
        if (fullId == null || fullId.isBlank()) {
            startNew();
            return;
        }

        this.headerTitleLabel.setText(fullId);
        this.headerSubLabel.setText("Editing chemical properties");

        int colon = fullId.indexOf(':');
        String ns = colon > 0 ? fullId.substring(0, colon) : "kubejs";
        String path = colon > 0 ? fullId.substring(colon + 1) : fullId;

        this.nsField.setText(ns);
        this.pathField.setText(path);
        this.displayNameField.setText(path);
        this.statusLabel.setText("Selected " + fullId);
    }

    private static UIElement createDivider() {
        UIElement d = new UIElement();
        d.addClass("divider");
        d.layout(l -> {
            l.widthPercent(100.0f);
            l.height(1.0f);
        });
        return d;
    }

    private static Label createFieldLabel(String text) {
        Label l = new Label();
        l.setText(text);
        l.layout(layout -> layout.width(LABEL_WIDTH));
        return l;
    }

    private static UIElement createFieldRow(String labelText, UIElement fieldElement) {
        UIElement row = new UIElement();
        row.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
            l.gapAll(6.0f);
            l.alignItems(AlignItems.CENTER);
            l.widthPercent(100.0f);
        });

        row.addChildren(createFieldLabel(labelText), fieldElement);
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

    private void handleRevert() {
        startNew();
        this.statusLabel.setText("Reverted changes");
    }

    private void handleQueue() {
        String ns = this.nsField.getValue() != null ? this.nsField.getValue().trim() : "kubejs";
        String path = this.pathField.getValue() != null ? this.pathField.getValue().trim() : "";
        if (path.isEmpty()) {
            this.statusLabel.setText("§cPath cannot be empty / IDのパスを入力してください");
            return;
        }

        String fullId = ns + ":" + path;
        String displayName = this.displayNameField.getValue() != null ? this.displayNameField.getValue().trim() : "";
        if (displayName.isEmpty()) {
            displayName = path;
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

        String selectedType = this.typeSelector.getValue();
        if (selectedType == null || selectedType.isBlank()) selectedType = "gas";

        MekanismGasDefinition def = MekanismGasDefinition.builder(fullId)
                .chemicalType(selectedType)
                .displayName(displayName)
                .color(parsedColor)
                .radioactivity(radioactivity)
                .texture(texture)
                .tags(tags)
                .build();

        StudioWorkspaceHelper.queueGasIntoWorkspace(def);
        GasCreateUi.queuePendingGas(def);

        this.statusLabel.setText("§aQueued '" + fullId + "'! Saved to startup_scripts.");

        if (this.onQueuedCallback != null) {
            try {
                this.onQueuedCallback.run();
            } catch (Throwable t) {
                KubeJSStudioMekanismCompat.LOGGER.warn("Error running onQueuedCallback: {}", t.getMessage());
            }
        }
    }
}
