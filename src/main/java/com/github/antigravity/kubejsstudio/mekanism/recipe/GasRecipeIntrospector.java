package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Universal recipe introspector that inspects arbitrary recipe JSON structures (from Mekanism,
 * addons, or KubeJS-created custom machines) and automatically identifies gas/item/fluid inputs,
 * outputs, and numeric/boolean parameters without hardcoding machine types.
 */
public final class GasRecipeIntrospector {

    public enum SlotType {
        ITEM_IN, ITEM_OUT,
        GAS_IN, GAS_OUT,
        FLUID_IN, FLUID_OUT
    }

    public record DetectedSlot(
            String keyPath,
            SlotType type,
            String label,
            int col,
            int row,
            String valueIdOrTag,
            int amount
    ) {}

    public record DetectedField(
            String keyPath,
            String label,
            String fieldType, // "INT", "FLOAT", "BOOL", "TEXT"
            String defaultValue
    ) {}

    public record IntrospectionResult(
            String typeId,
            List<DetectedSlot> slots,
            List<DetectedField> fields,
            boolean containsGas
    ) {}

    private static final Set<String> GAS_KEYS = Set.of(
            "gas", "gasinput", "gas_input", "chemicalinput", "chemical_input",
            "gasoutput", "gas_output", "chemicaloutput", "chemical_output",
            "gastag", "gas_tag"
    );

    private static final Set<String> FLUID_KEYS = Set.of(
            "fluid", "fluidinput", "fluid_input", "fluidoutput", "fluid_output",
            "fluidtag", "fluid_tag"
    );

    private static final Set<String> ITEM_KEYS = Set.of(
            "item", "iteminput", "item_input", "itemoutput", "item_output",
            "ingredient", "ingredients", "maininput", "extrainput",
            "result", "results", "tag"
    );

    private GasRecipeIntrospector() {}

    /**
     * Inspects any arbitrary recipe JSON and detects slots and fields dynamically.
     */
    public static IntrospectionResult inspect(JsonObject json) {
        String typeId = json.has("type") ? json.get("type").getAsString() : "custom:unknown";

        List<DetectedSlot> slots = new ArrayList<>();
        List<DetectedField> fields = new ArrayList<>();
        boolean[] hasGas = new boolean[]{false};

        int[] inRow = new int[]{0};
        int[] outRow = new int[]{0};

        // Scan top-level keys
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            if ("type".equals(key) || "conditions".equals(key) || "forge:conditions".equals(key)) {
                continue;
            }

            JsonElement val = entry.getValue();
            String lowerKey = key.toLowerCase();

            if (val.isJsonObject()) {
                inspectObject(key, val.getAsJsonObject(), slots, fields, hasGas, inRow, outRow);
            } else if (val.isJsonArray()) {
                inspectArray(key, val.getAsJsonArray(), slots, fields, hasGas, inRow, outRow);
            } else if (val.isJsonPrimitive()) {
                JsonPrimitive p = val.getAsJsonPrimitive();
                if (p.isNumber()) {
                    String fieldType = p.getAsString().contains(".") ? "FLOAT" : "INT";
                    fields.add(new DetectedField(key, formatLabel(key), fieldType, p.getAsString()));
                } else if (p.isBoolean()) {
                    fields.add(new DetectedField(key, formatLabel(key), "BOOL", p.getAsString()));
                } else if (p.isString()) {
                    // Check if it's a simple string pointing to an item, gas, or fluid
                    classifyStringPrimitive(key, p.getAsString(), slots, fields, hasGas, inRow, outRow);
                }
            }
        }

        return new IntrospectionResult(typeId, slots, fields, hasGas[0]);
    }

    private static void inspectObject(
            String parentKey, JsonObject obj,
            List<DetectedSlot> slots, List<DetectedField> fields,
            boolean[] hasGas, int[] inRow, int[] outRow
    ) {
        String lowerParent = parentKey.toLowerCase();
        boolean isOutput = lowerParent.contains("output") || lowerParent.contains("result");

        // Check for Mekanism Chemical/Gas Stack: { "gas": "...", "amount": ... } or { "gasTag": "..." }
        if (obj.has("gas") || obj.has("gasTag") || obj.has("chemicalType") || lowerParent.contains("gas") || lowerParent.contains("chemical")) {
            hasGas[0] = true;
            String gasId = "";
            if (obj.has("gas")) gasId = obj.get("gas").getAsString();
            else if (obj.has("gasTag")) gasId = "#" + obj.get("gasTag").getAsString();
            else if (obj.has("tag")) gasId = "#" + obj.get("tag").getAsString();

            int amount = 1;
            if (obj.has("amount")) amount = obj.get("amount").getAsInt();

            SlotType slotType = isOutput ? SlotType.GAS_OUT : SlotType.GAS_IN;
            int col = isOutput ? 2 : 0;
            int row = isOutput ? outRow[0]++ : inRow[0]++;

            slots.add(new DetectedSlot(parentKey, slotType, formatLabel(parentKey), col, row, gasId, amount));
            return;
        }

        // Check for Fluid Stack: { "fluid": "...", "amount": ... }
        if (obj.has("fluid") || obj.has("fluidTag") || lowerParent.contains("fluid")) {
            String fluidId = "";
            if (obj.has("fluid")) fluidId = obj.get("fluid").getAsString();
            else if (obj.has("fluidTag")) fluidId = "#" + obj.get("fluidTag").getAsString();

            int amount = 1000;
            if (obj.has("amount")) amount = obj.get("amount").getAsInt();

            SlotType slotType = isOutput ? SlotType.FLUID_OUT : SlotType.FLUID_IN;
            int col = isOutput ? 2 : 0;
            int row = isOutput ? outRow[0]++ : inRow[0]++;

            slots.add(new DetectedSlot(parentKey, slotType, formatLabel(parentKey), col, row, fluidId, amount));
            return;
        }

        // Check for Item Stack: { "item": "...", "count": ... } or { "tag": "..." }
        if (obj.has("item") || obj.has("tag") || obj.has("ingredient")) {
            String itemId = "";
            if (obj.has("item")) itemId = obj.get("item").getAsString();
            else if (obj.has("tag")) itemId = "#" + obj.get("tag").getAsString();
            else if (obj.has("ingredient") && obj.get("ingredient").isJsonObject()) {
                JsonObject ing = obj.getAsJsonObject("ingredient");
                if (ing.has("item")) itemId = ing.get("item").getAsString();
                else if (ing.has("tag")) itemId = "#" + ing.get("tag").getAsString();
            }

            int count = 1;
            if (obj.has("count")) count = obj.get("count").getAsInt();
            else if (obj.has("amount")) count = obj.get("amount").getAsInt();

            SlotType slotType = isOutput ? SlotType.ITEM_OUT : SlotType.ITEM_IN;
            int col = isOutput ? 2 : 0;
            int row = isOutput ? outRow[0]++ : inRow[0]++;

            slots.add(new DetectedSlot(parentKey, slotType, formatLabel(parentKey), col, row, itemId, count));
            return;
        }

        // If not a recognized single stack, recurse into children
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            String childKey = parentKey + "." + e.getKey();
            if (e.getValue().isJsonObject()) {
                inspectObject(childKey, e.getValue().getAsJsonObject(), slots, fields, hasGas, inRow, outRow);
            } else if (e.getValue().isJsonPrimitive()) {
                JsonPrimitive p = e.getValue().getAsJsonPrimitive();
                if (p.isNumber()) {
                    fields.add(new DetectedField(childKey, formatLabel(childKey), p.getAsString().contains(".") ? "FLOAT" : "INT", p.getAsString()));
                } else if (p.isBoolean()) {
                    fields.add(new DetectedField(childKey, formatLabel(childKey), "BOOL", p.getAsString()));
                }
            }
        }
    }

    private static void inspectArray(
            String parentKey, JsonArray arr,
            List<DetectedSlot> slots, List<DetectedField> fields,
            boolean[] hasGas, int[] inRow, int[] outRow
    ) {
        for (int i = 0; i < arr.size(); i++) {
            JsonElement el = arr.get(i);
            String itemKey = parentKey + "[" + i + "]";
            if (el.isJsonObject()) {
                inspectObject(itemKey, el.getAsJsonObject(), slots, fields, hasGas, inRow, outRow);
            }
        }
    }

    private static void classifyStringPrimitive(
            String key, String value,
            List<DetectedSlot> slots, List<DetectedField> fields,
            boolean[] hasGas, int[] inRow, int[] outRow
    ) {
        String lowerKey = key.toLowerCase();
        boolean isOutput = lowerKey.contains("output") || lowerKey.contains("result");

        if (lowerKey.contains("gas") || lowerKey.contains("chemical")) {
            hasGas[0] = true;
            SlotType slotType = isOutput ? SlotType.GAS_OUT : SlotType.GAS_IN;
            int col = isOutput ? 2 : 0;
            int row = isOutput ? outRow[0]++ : inRow[0]++;
            slots.add(new DetectedSlot(key, slotType, formatLabel(key), col, row, value, 1));
        } else if (lowerKey.contains("fluid")) {
            SlotType slotType = isOutput ? SlotType.FLUID_OUT : SlotType.FLUID_IN;
            int col = isOutput ? 2 : 0;
            int row = isOutput ? outRow[0]++ : inRow[0]++;
            slots.add(new DetectedSlot(key, slotType, formatLabel(key), col, row, value, 1000));
        } else if (lowerKey.contains("item") || lowerKey.contains("ingredient") || isOutput) {
            SlotType slotType = isOutput ? SlotType.ITEM_OUT : SlotType.ITEM_IN;
            int col = isOutput ? 2 : 0;
            int row = isOutput ? outRow[0]++ : inRow[0]++;
            slots.add(new DetectedSlot(key, slotType, formatLabel(key), col, row, value, 1));
        } else {
            fields.add(new DetectedField(key, formatLabel(key), "TEXT", value));
        }
    }

    private static String formatLabel(String rawKey) {
        String name = rawKey.contains(".") ? rawKey.substring(rawKey.lastIndexOf('.') + 1) : rawKey;
        name = name.replaceAll("\\[\\d+\\]", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append(' ');
            } else if (c == '_' || c == '-') {
                sb.append(' ');
                continue;
            }
            sb.append(i == 0 ? Character.toUpperCase(c) : c);
        }
        return sb.toString().trim();
    }
}
