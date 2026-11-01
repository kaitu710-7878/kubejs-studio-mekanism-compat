package com.github.antigravity.kubejsstudio.mekanism.client;

import com.github.antigravity.kubejsstudio.mekanism.KubeJSStudioMekanismCompat;
import com.github.antigravity.kubejsstudio.mekanism.gas.GasScriptSerializer;
import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller and state manager for Mekanism Gas visual creation panel in KubeJS Studio.
 * Seamlessly integrates into the Create tab and manages pending gas registry operations.
 */
public final class GasCreateUi {
    private static final Map<String, MekanismGasDefinition> MANAGED_GASES = new LinkedHashMap<>();
    private static final Map<String, MekanismGasDefinition> PENDING_GASES = new LinkedHashMap<>();

    public static void queuePendingGas(MekanismGasDefinition gas) {
        if (gas != null && !gas.id().isBlank()) {
            PENDING_GASES.put(gas.id(), gas);
            KubeJSStudioMekanismCompat.LOGGER.info("Queued pending gas into GasCreateUi: {}", gas.id());
        }
    }

    private String currentNamespace = "kubejs";
    private String currentPath = "";
    private String currentDisplayName = "";
    private int currentColor = 0x00FFCC;
    private String currentTexture = "";
    private double currentRadioactivity = 0.0;
    private List<String> currentTags = new ArrayList<>();
    private String selectedId = "";

    public GasCreateUi() {
        reloadFromDisk();
    }

    public void startNew() {
        this.selectedId = "";
        this.currentNamespace = "kubejs";
        this.currentPath = "";
        this.currentDisplayName = "";
        this.currentColor = 0x00FFCC;
        this.currentTexture = "";
        this.currentRadioactivity = 0.0;
        this.currentTags.clear();
    }

    public void selectGas(String fullId) {
        if (fullId == null || fullId.isBlank()) {
            startNew();
            return;
        }

        MekanismGasDefinition def = PENDING_GASES.get(fullId);
        if (def == null) def = MANAGED_GASES.get(fullId);

        if (def != null) {
            this.selectedId = def.id();
            int colon = def.id().indexOf(':');
            this.currentNamespace = colon > 0 ? def.id().substring(0, colon) : "kubejs";
            this.currentPath = colon > 0 ? def.id().substring(colon + 1) : def.id();
            this.currentDisplayName = def.displayName();
            this.currentColor = def.color();
            this.currentTexture = def.texture();
            this.currentRadioactivity = def.radioactivity();
            this.currentTags = new ArrayList<>(def.tags());
        }
    }

    /**
     * Queues the current gas configuration into the pending list.
     * Returns true if successfully queued, or false if validation failed.
     */
    public boolean queueCurrent() {
        if (currentNamespace.isBlank() || currentPath.isBlank()) {
            return false;
        }

        String fullId = currentNamespace.toLowerCase().trim() + ":" + currentPath.toLowerCase().trim();
        MekanismGasDefinition gas = new MekanismGasDefinition(
                fullId,
                currentDisplayName,
                currentColor,
                currentTexture,
                currentRadioactivity,
                currentTags
        );

        PENDING_GASES.put(fullId, gas);
        KubeJSStudioMekanismCompat.LOGGER.info("Queued custom Mekanism gas: {}", fullId);
        return true;
    }

    /**
     * Removes the currently selected gas from pending or queues its deletion.
     */
    public void deleteSelected() {
        if (!selectedId.isBlank()) {
            PENDING_GASES.remove(selectedId);
            MANAGED_GASES.remove(selectedId);
            startNew();
        }
    }

    /**
     * Reads existing gas registration scripts under kubejs/startup_scripts/
     */
    public void reloadFromDisk() {
        MANAGED_GASES.clear();
        try {
            Path scriptFile = Path.of("kubejs", "startup_scripts", "gases.js");
            if (Files.exists(scriptFile)) {
                String content = Files.readString(scriptFile);
                List<MekanismGasDefinition> parsed = GasScriptSerializer.parseScript(content);
                for (MekanismGasDefinition g : parsed) {
                    MANAGED_GASES.put(g.id(), g);
                }
            }
        } catch (IOException e) {
            KubeJSStudioMekanismCompat.LOGGER.warn("Could not read back existing gases.js: {}", e.getMessage());
        }
    }

    /**
     * Writes all managed and pending gases to kubejs/startup_scripts/gases.js.
     */
    public boolean applyPendingToDisk() {
        Map<String, MekanismGasDefinition> merged = new LinkedHashMap<>(MANAGED_GASES);
        merged.putAll(PENDING_GASES);

        if (merged.isEmpty()) {
            return true;
        }

        try {
            Path dir = Path.of("kubejs", "startup_scripts");
            Files.createDirectories(dir);
            Path target = dir.resolve("gases.js");
            String script = GasScriptSerializer.buildStartupScript(merged.values());
            Files.writeString(target, script);

            MANAGED_GASES.putAll(PENDING_GASES);
            PENDING_GASES.clear();
            KubeJSStudioMekanismCompat.LOGGER.info("Successfully wrote {} gases to {}", merged.size(), target);
            return true;
        } catch (IOException e) {
            KubeJSStudioMekanismCompat.LOGGER.error("Failed to write gases.js: {}", e.getMessage());
            return false;
        }
    }

    // Getters and Setters
    public String currentNamespace() { return currentNamespace; }
    public void setCurrentNamespace(String ns) { this.currentNamespace = ns; }

    public String currentPath() { return currentPath; }
    public void setCurrentPath(String path) { this.currentPath = path; }

    public String currentDisplayName() { return currentDisplayName; }
    public void setCurrentDisplayName(String name) { this.currentDisplayName = name; }

    public int currentColor() { return currentColor; }
    public void setCurrentColor(int color) { this.currentColor = color; }

    public String currentTexture() { return currentTexture; }
    public void setCurrentTexture(String texture) { this.currentTexture = texture; }

    public double currentRadioactivity() { return currentRadioactivity; }
    public void setCurrentRadioactivity(double radioactivity) { this.currentRadioactivity = radioactivity; }

    public List<String> currentTags() { return currentTags; }
    public void setCurrentTags(List<String> tags) { this.currentTags = new ArrayList<>(tags); }

    public void setTagsFromString(String tagCsv) {
        this.currentTags.clear();
        if (tagCsv != null && !tagCsv.isBlank()) {
            for (String t : tagCsv.split(",")) {
                if (!t.trim().isBlank()) {
                    this.currentTags.add(t.trim());
                }
            }
        }
    }

    public String selectedId() { return selectedId; }
    public Map<String, MekanismGasDefinition> pendingGases() { return PENDING_GASES; }
    public Map<String, MekanismGasDefinition> managedGases() { return MANAGED_GASES; }
}
