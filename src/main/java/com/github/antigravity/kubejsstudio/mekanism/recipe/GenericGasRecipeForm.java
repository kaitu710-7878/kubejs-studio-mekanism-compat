package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Universal recipe form implementation that dynamically handles any gas-capable machine recipe,
 * including addon machines and KubeJS-created custom machines.
 */
public final class GenericGasRecipeForm {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final String typeId;
    private final String label;
    private final List<GasRecipeIntrospector.DetectedSlot> slots;
    private final List<GasRecipeIntrospector.DetectedField> fields;

    public GenericGasRecipeForm(String typeId, String label, List<GasRecipeIntrospector.DetectedSlot> slots, List<GasRecipeIntrospector.DetectedField> fields) {
        this.typeId = Objects.requireNonNull(typeId, "typeId cannot be null");
        this.label = (label != null && !label.isBlank()) ? label : formatLabelFromTypeId(typeId);
        this.slots = List.copyOf(slots);
        this.fields = List.copyOf(fields);
    }

    public static GenericGasRecipeForm fromIntrospection(GasRecipeIntrospector.IntrospectionResult result) {
        return new GenericGasRecipeForm(result.typeId(), formatLabelFromTypeId(result.typeId()), result.slots(), result.fields());
    }

    public String typeId() { return typeId; }
    public String label() { return label; }
    public List<GasRecipeIntrospector.DetectedSlot> slots() { return slots; }
    public List<GasRecipeIntrospector.DetectedField> fields() { return fields; }

    /**
     * Reconstructs JSON output dynamically based on slot and field values.
     */
    public String toJson(List<String> slotValues, Map<String, String> fieldValues) {
        JsonObject root = new JsonObject();
        root.addProperty("type", typeId);

        // Populate slots
        for (int i = 0; i < slots.size(); i++) {
            GasRecipeIntrospector.DetectedSlot slot = slots.get(i);
            String val = (i < slotValues.size()) ? slotValues.get(i) : "";
            if (val == null || val.isBlank()) continue;

            int amount = slot.amount();
            String amountKey = slot.keyPath() + ".amount";
            if (fieldValues.containsKey(amountKey)) {
                try {
                    amount = Integer.parseInt(fieldValues.get(amountKey).trim());
                } catch (NumberFormatException ignored) {}
            }

            JsonElement node = buildSlotNode(slot.type(), val, amount);
            insertByPath(root, slot.keyPath(), node);
        }

        // Populate fields
        for (GasRecipeIntrospector.DetectedField f : fields) {
            String val = fieldValues.get(f.keyPath());
            if (val == null || val.isBlank()) val = f.defaultValue();
            if (val != null && !val.isBlank()) {
                JsonElement elem = switch (f.fieldType()) {
                    case "INT" -> {
                        try { yield new com.google.gson.JsonPrimitive(Integer.parseInt(val.trim())); }
                        catch (Exception e) { yield new com.google.gson.JsonPrimitive(val); }
                    }
                    case "FLOAT" -> {
                        try { yield new com.google.gson.JsonPrimitive(Double.parseDouble(val.trim())); }
                        catch (Exception e) { yield new com.google.gson.JsonPrimitive(val); }
                    }
                    case "BOOL" -> new com.google.gson.JsonPrimitive(Boolean.parseBoolean(val.trim()));
                    default -> new com.google.gson.JsonPrimitive(val);
                };
                insertByPath(root, f.keyPath(), elem);
            }
        }

        return PRETTY.toJson(root);
    }

    /**
     * Converts to universal KubeJS recipe script statement.
     * Uses event.recipes.<namespace>.<path>(...) if simple, or event.custom({...}) for arbitrary machines.
     */
    public String toKubeJsScript(List<String> slotValues, Map<String, String> fieldValues) {
        String json = toJson(slotValues, fieldValues);
        int colon = typeId.indexOf(':');
        String ns = colon > 0 ? typeId.substring(0, colon) : "custom";
        String path = colon > 0 ? typeId.substring(colon + 1) : typeId;

        // Clean method name: e.g. "purifying" -> event.recipes.mekanism.purifying(...)
        if ("mekanism".equals(ns)) {
            // For standard Mekanism machines, produce neat method calls
            MekanismGasRecipeType knownType = MekanismGasRecipeType.byTypeId(typeId);
            if (knownType != null) {
                return buildKnownMekanismScript(knownType, slotValues, fieldValues);
            }
        }

        // Universal fallback for addon machines or KubeJS custom machines:
        // produces clean and infallible event.custom(...) call
        return "event.custom(" + json + ");";
    }

    private static String buildKnownMekanismScript(MekanismGasRecipeType type, List<String> slotValues, Map<String, String> fieldValues) {
        StringBuilder sb = new StringBuilder("event.recipes.mekanism.").append(type.shortName()).append("(");
        List<String> args = new ArrayList<>();
        for (String sv : slotValues) {
            if (sv != null && !sv.isBlank()) {
                args.add("'" + sv + "'");
            }
        }
        sb.append(String.join(", ", args)).append(");");
        return sb.toString();
    }

    private static JsonElement buildSlotNode(GasRecipeIntrospector.SlotType type, String idOrTag, int amount) {
        JsonObject obj = new JsonObject();
        boolean isTag = idOrTag.startsWith("#");
        String clean = isTag ? idOrTag.substring(1) : idOrTag;

        switch (type) {
            case GAS_IN, GAS_OUT -> {
                if (isTag) obj.addProperty("gasTag", clean);
                else obj.addProperty("gas", clean);
                if (amount > 1) obj.addProperty("amount", amount);
                return obj;
            }
            case FLUID_IN, FLUID_OUT -> {
                if (isTag) obj.addProperty("fluidTag", clean);
                else obj.addProperty("fluid", clean);
                if (amount > 1) obj.addProperty("amount", amount);
                return obj;
            }
            case ITEM_IN, ITEM_OUT -> {
                if (isTag) obj.addProperty("tag", clean);
                else obj.addProperty("item", clean);
                if (amount > 1) obj.addProperty("amount", amount);
                return obj;
            }
        }
        return obj;
    }

    private static void insertByPath(JsonObject root, String path, JsonElement element) {
        String[] parts = path.split("\\.");
        JsonObject curr = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String p = parts[i];
            if (!curr.has(p) || !curr.get(p).isJsonObject()) {
                JsonObject next = new JsonObject();
                curr.add(p, next);
                curr = next;
            } else {
                curr = curr.getAsJsonObject(p);
            }
        }
        curr.add(parts[parts.length - 1], element);
    }

    private static String formatLabelFromTypeId(String typeId) {
        int colon = typeId.indexOf(':');
        String name = colon > 0 ? typeId.substring(colon + 1) : typeId;
        String[] parts = name.split("[_-]");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isBlank()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
