package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.tysontheember.kubejsstudio.core.op.RecipeForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Universal RecipeForm implementation for KubeJS Studio covering all Mekanism machines,
 * Astral Mekanism machines, and custom addon machines.
 */
public class UniversalMekanismRecipeForm implements RecipeForm {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final String typeId;
    private final String label;
    private final List<SlotDef> slotDefs;
    private final List<Field> fields;

    public record SlotDef(RecipeForm.Slot slot, String jsonKeyPath, String defaultAmountKey, int defaultAmount) {}

    public UniversalMekanismRecipeForm(String typeId, String label, List<SlotDef> slotDefs, List<Field> fields) {
        this.typeId = Objects.requireNonNull(typeId, "typeId cannot be null");
        this.label = (label != null && !label.isBlank()) ? label : formatLabel(typeId);
        this.slotDefs = List.copyOf(slotDefs);
        this.fields = List.copyOf(fields);
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String typeId() {
        return typeId;
    }

    @Override
    public String namespace() {
        int colon = typeId.indexOf(':');
        return colon > 0 ? typeId.substring(0, colon) : "mekanism";
    }

    @Override
    public List<RecipeForm.Slot> slots() {
        List<RecipeForm.Slot> result = new ArrayList<>();
        for (SlotDef def : slotDefs) {
            result.add(def.slot());
        }
        return result;
    }

    @Override
    public List<RecipeForm.Field> fields() {
        return fields;
    }

    @Override
    public RecipeForm.Parsed parse(JsonObject json) {
        if (json == null) return null;

        List<String> slotVals = new ArrayList<>();
        for (int i = 0; i < slotDefs.size(); i++) {
            slotVals.add("");
        }
        Map<String, String> fieldVals = new HashMap<>();

        // Parse each slot by its jsonKeyPath
        for (int i = 0; i < slotDefs.size(); i++) {
            SlotDef def = slotDefs.get(i);
            String val = extractValueByPath(json, def.jsonKeyPath());
            if (val != null) {
                slotVals.set(i, val);
            }

            // Extract amount if specified
            if (def.defaultAmountKey() != null) {
                int amt = extractAmountByPath(json, def.jsonKeyPath());
                if (amt > 0) {
                    fieldVals.put(def.defaultAmountKey(), String.valueOf(amt));
                }
            }
        }

        // Parse fields (duration, energy, etc.)
        for (RecipeForm.Field f : fields) {
            if (json.has(f.key())) {
                fieldVals.put(f.key(), json.get(f.key()).getAsString());
            }
        }

        return new RecipeForm.Parsed(slotVals, fieldVals);
    }

    @Override
    public String toJson(List<String> slotValues, Map<String, String> fieldValues) {
        JsonObject root = new JsonObject();
        root.addProperty("type", typeId);

        for (int i = 0; i < slotDefs.size(); i++) {
            SlotDef def = slotDefs.get(i);
            String val = (i < slotValues.size()) ? slotValues.get(i) : "";
            if (val == null || val.isBlank()) continue;

            int amount = def.defaultAmount();
            if (def.defaultAmountKey() != null && fieldValues.containsKey(def.defaultAmountKey())) {
                try {
                    amount = Integer.parseInt(fieldValues.get(def.defaultAmountKey()).trim());
                } catch (NumberFormatException ignored) {}
            }

            JsonElement node = buildNodeForRole(def.slot().role(), def.jsonKeyPath(), val, amount);
            insertByPath(root, def.jsonKeyPath(), node);
        }

        for (RecipeForm.Field f : fields) {
            String val = fieldValues.get(f.key());
            if (val != null && !val.isBlank()) {
                switch (f.kind()) {
                    case INT -> {
                        try { root.addProperty(f.key(), Integer.parseInt(val.trim())); }
                        catch (Exception e) { root.addProperty(f.key(), val); }
                    }
                    case FLOAT -> {
                        try { root.addProperty(f.key(), Double.parseDouble(val.trim())); }
                        catch (Exception e) { root.addProperty(f.key(), val); }
                    }
                    case BOOL -> root.addProperty(f.key(), Boolean.parseBoolean(val.trim()));
                    default -> root.addProperty(f.key(), val);
                }
            }
        }

        return PRETTY.toJson(root);
    }

    @Override
    public String toKube(List<String> slotValues, Map<String, String> fieldValues) {
        String json = toJson(slotValues, fieldValues);
        // Format as event.recipes.mekanism.<shortName>(...) if applicable, or event.custom({...})
        int colon = typeId.indexOf(':');
        String ns = colon > 0 ? typeId.substring(0, colon) : "mekanism";
        String path = colon > 0 ? typeId.substring(colon + 1) : typeId;

        if ("mekanism".equals(ns)) {
            List<String> nonBlank = new ArrayList<>();
            for (String sv : slotValues) {
                if (sv != null && !sv.isBlank()) nonBlank.add("'" + sv + "'");
            }
            if (!nonBlank.isEmpty()) {
                return "event.recipes.mekanism." + path + "(" + String.join(", ", nonBlank) + ");";
            }
        }

        return "event.custom(" + json + ");";
    }

    private static String extractValueByPath(JsonObject json, String path) {
        JsonElement elem = getByPath(json, path);
        if (elem == null || elem.isJsonNull()) return null;

        if (elem.isJsonPrimitive()) {
            return elem.getAsString();
        }

        if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.has("ingredient")) {
                return extractValueByPath(obj, "ingredient");
            }
            if (obj.has("item")) return obj.get("item").getAsString();
            if (obj.has("tag")) return "#" + obj.get("tag").getAsString();
            if (obj.has("gas")) return obj.get("gas").getAsString();
            if (obj.has("gasTag")) return "#" + obj.get("gasTag").getAsString();
            if (obj.has("fluid")) return obj.get("fluid").getAsString();
            if (obj.has("fluidTag")) return "#" + obj.get("fluidTag").getAsString();
            if (obj.has("FluidName")) return obj.get("FluidName").getAsString();
            if (obj.has("fluidStack")) return extractValueByPath(obj, "fluidStack");
            if (obj.has("infuse_type")) return obj.get("infuse_type").getAsString();
            if (obj.has("slurry")) return obj.get("slurry").getAsString();
            if (obj.has("id")) return obj.get("id").getAsString();
        }

        if (elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            if (!arr.isEmpty()) {
                JsonElement first = arr.get(0);
                if (first.isJsonObject()) {
                    return extractValueByPath(first.getAsJsonObject(), "");
                } else if (first.isJsonPrimitive()) {
                    return first.getAsString();
                }
            }
        }

        return null;
    }

    private static int extractAmountByPath(JsonObject json, String path) {
        JsonElement elem = getByPath(json, path);
        if (elem != null && elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.has("amount")) return obj.get("amount").getAsInt();
            if (obj.has("count")) return obj.get("count").getAsInt();
            if (obj.has("Amount")) return obj.get("Amount").getAsInt();
            if (obj.has("#")) return obj.get("#").getAsInt();
        }
        return 0;
    }

    private static JsonElement getByPath(JsonObject root, String path) {
        if (path == null || path.isBlank()) return root;
        String[] parts = path.split("\\.");
        JsonElement curr = root;
        for (String p : parts) {
            if (curr != null && curr.isJsonObject()) {
                curr = curr.getAsJsonObject().get(p);
            } else {
                return null;
            }
        }
        return curr;
    }

    private static JsonElement buildNodeForRole(RecipeForm.Role role, String keyPath, String idOrTag, int amount) {
        JsonObject obj = new JsonObject();
        boolean isTag = idOrTag.startsWith("#");
        String clean = isTag ? idOrTag.substring(1) : idOrTag;

        boolean isChemical = (role == RecipeForm.Role.FLUID_IN || role == RecipeForm.Role.FLUID_OUT);

        if (isChemical) {
            if (keyPath.contains("infus")) {
                obj.addProperty(isTag ? "tag" : "infuse_type", clean);
            } else if (keyPath.contains("slurry")) {
                obj.addProperty(isTag ? "tag" : "slurry", clean);
            } else if (keyPath.contains("fluid")) {
                obj.addProperty(isTag ? "tag" : "fluid", clean);
            } else {
                obj.addProperty(isTag ? "tag" : "gas", clean);
            }
        } else {
            // Item
            if (keyPath.contains("itemInput") || keyPath.contains("ingredient") || keyPath.contains("input")) {
                JsonObject ing = new JsonObject();
                ing.addProperty(isTag ? "tag" : "item", clean);
                obj.add("ingredient", ing);
            } else {
                obj.addProperty(isTag ? "tag" : "item", clean);
            }
        }

        if (amount > 1) {
            obj.addProperty("amount", amount);
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

    private static String formatLabel(String typeId) {
        int colon = typeId.indexOf(':');
        String ns = colon > 0 ? typeId.substring(0, colon) : "";
        String name = colon > 0 ? typeId.substring(colon + 1) : typeId;
        String[] parts = name.split("[_-]");
        StringBuilder sb = new StringBuilder();
        if (!ns.isBlank() && !"mekanism".equals(ns)) {
            sb.append(Character.toUpperCase(ns.charAt(0))).append(ns.substring(1)).append(": ");
        }
        for (String p : parts) {
            if (!p.isBlank()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
