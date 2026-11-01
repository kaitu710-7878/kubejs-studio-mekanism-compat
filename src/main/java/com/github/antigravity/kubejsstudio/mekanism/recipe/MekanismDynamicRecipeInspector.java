package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.tysontheember.kubejsstudio.core.op.RecipeForm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamically inspects any recipe JSON (Mekanism, Astral Mekanism, Addons)
 * and generates a pixel-perfect, tailored UniversalMekanismRecipeForm containing
 * ONLY the slots and fields that actually exist in the recipe JSON.
 *
 * This completely eliminates phantom gas slots on 1-in 1-out machines (e.g. enriching chamber)
 * and accurately surfaces all items, fluids, and gases for complex multi-IO machines
 * (e.g. mechanical transform, astral crafting, infusing condense).
 */
public final class MekanismDynamicRecipeInspector {

    private MekanismDynamicRecipeInspector() {}

    public static UniversalMekanismRecipeForm inspect(String typeId, JsonObject json) {
        if (json == null) {
            return null;
        }

        List<UniversalMekanismRecipeForm.SlotDef> slots = new ArrayList<>();
        List<RecipeForm.Field> fields = new ArrayList<>();

        // Check for Mekanism MekData 3x3 Crafting (jetpack, laser, etc.)
        if ("mekanism:mek_data".equals(typeId) || (json.has("pattern") && json.has("key"))) {
            return buildMekDataCraftingForm(typeId, json);
        }

        // Check for 5x5 Astral Crafting grid (itemInputA1 .. itemInputE5)
        boolean isGridCrafting = json.has("itemInputA1") || json.has("itemInputC3");
        if (isGridCrafting) {
            return buildAstralCraftingGridForm(typeId, json);
        }

        // Standard inspection of inputs and outputs
        List<DetectedSlot> detectedInputs = new ArrayList<>();
        List<DetectedSlot> detectedOutputs = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement val = entry.getValue();
            if ("type".equals(key)) continue;

            if (val.isJsonObject() || val.isJsonArray()) {
                DetectedSlot slot = classifySlot(key, val);
                if (slot != null) {
                    if (slot.isOutput) {
                        detectedOutputs.add(slot);
                    } else {
                        detectedInputs.add(slot);
                    }
                }
            } else if (val.isJsonPrimitive()) {
                // Fields (duration, energy, etc.)
                String lk = key.toLowerCase();
                if (lk.contains("duration") || lk.contains("time") || lk.contains("ticks")) {
                    fields.add(new RecipeForm.Field(key, formatLabel(key), RecipeForm.Field.Kind.INT, false));
                } else if (lk.contains("energy")) {
                    fields.add(new RecipeForm.Field(key, formatLabel(key), RecipeForm.Field.Kind.INT, false));
                } else if (lk.contains("catalyst") || val.getAsJsonPrimitive().isBoolean()) {
                    fields.add(new RecipeForm.Field(key, formatLabel(key), RecipeForm.Field.Kind.BOOL, true));
                }
            }
        }

        // Sort detected slots: Items first, then Fluids / Chemicals for intuitive visual layout
        detectedInputs.sort(Comparator.comparingInt(s -> s.role == RecipeForm.Role.INPUT ? 0 : 1));
        detectedOutputs.sort(Comparator.comparingInt(s -> s.role == RecipeForm.Role.OUTPUT ? 0 : 1));

        // Layout the slots in col/row grid
        int inputRow = 0;
        int inputCol = 0;
        for (DetectedSlot in : detectedInputs) {
            slots.add(new UniversalMekanismRecipeForm.SlotDef(
                    new RecipeForm.Slot(in.role, in.label, inputCol, inputRow),
                    in.keyPath, in.amtKey, in.defAmt
            ));
            inputRow++;
            if (inputRow >= 3) {
                inputRow = 0;
                inputCol++;
            }
        }

        int outputCol = Math.max(2, inputCol + 2);
        int outputRow = 0;
        for (DetectedSlot out : detectedOutputs) {
            slots.add(new UniversalMekanismRecipeForm.SlotDef(
                    new RecipeForm.Slot(out.role, out.label, outputCol, outputRow),
                    out.keyPath, out.amtKey, out.defAmt
            ));
            outputRow++;
        }

        // Fallback: If absolutely no slots were detected, return standard fallback
        if (slots.isEmpty()) {
            return null;
        }

        return new UniversalMekanismRecipeForm(typeId, null, slots, fields);
    }

    private static UniversalMekanismRecipeForm buildAstralCraftingGridForm(String typeId, JsonObject json) {
        List<UniversalMekanismRecipeForm.SlotDef> slots = new ArrayList<>();
        List<RecipeForm.Field> fields = new ArrayList<>();

        char[] rows = {'A', 'B', 'C', 'D', 'E'};
        for (int r = 0; r < 5; r++) {
            char rowChar = rows[r];
            for (int c = 1; c <= 5; c++) {
                String key = "itemInput" + rowChar + c;
                if (json.has(key)) {
                    slots.add(new UniversalMekanismRecipeForm.SlotDef(
                            new RecipeForm.Slot(RecipeForm.Role.INPUT, "Slot " + rowChar + c, c - 1, r),
                            key, null, 1
                    ));
                }
            }
        }

        // Fluids & Gases for crafting table
        int extraRow = 0;
        int extraCol = 6;
        if (json.has("fluidInput")) {
            slots.add(new UniversalMekanismRecipeForm.SlotDef(
                    new RecipeForm.Slot(RecipeForm.Role.FLUID_IN, "Fluid Input", extraCol, extraRow++),
                    "fluidInput", "fluidAmount", 1000
            ));
        }
        if (json.has("gasInput")) {
            slots.add(new UniversalMekanismRecipeForm.SlotDef(
                    new RecipeForm.Slot(RecipeForm.Role.FLUID_IN, "Gas Input", extraCol, extraRow++),
                    "gasInput", "gasAmount", 1000
            ));
        }

        // Output
        slots.add(new UniversalMekanismRecipeForm.SlotDef(
                new RecipeForm.Slot(RecipeForm.Role.OUTPUT, "Result", extraCol + 2, 2),
                "output", null, 1
        ));

        return new UniversalMekanismRecipeForm(typeId, "Astral: Crafting Table", slots, fields);
    }

    public static UniversalMekanismRecipeForm buildMekDataCraftingForm(String typeId, JsonObject json) {
        List<UniversalMekanismRecipeForm.SlotDef> slots = new ArrayList<>();
        // 3x3 Crafting Grid
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                slots.add(new UniversalMekanismRecipeForm.SlotDef(
                        new RecipeForm.Slot(RecipeForm.Role.INPUT, "Slot " + (r * 3 + c + 1), c, r),
                        "grid_" + (r * 3 + c), null, 1
                ));
            }
        }
        // Result slot at (col 4, row 1)
        slots.add(new UniversalMekanismRecipeForm.SlotDef(
                new RecipeForm.Slot(RecipeForm.Role.OUTPUT, "Result", 4, 1),
                "result", null, 1
        ));

        return new UniversalMekanismRecipeForm(typeId, "Mekanism: Data Crafting", slots, List.of()) {
            @Override
            public RecipeForm.Parsed parse(JsonObject j) {
                MekDataRecipeParser.ParsedMekData parsed = MekDataRecipeParser.parse(j);
                if (parsed == null) return super.parse(j);
                List<String> slotVals = new ArrayList<>();
                for (int i = 0; i < 9; i++) {
                    slotVals.add(parsed.grid()[i]);
                }
                slotVals.add(parsed.result());
                return new RecipeForm.Parsed(slotVals, Map.of());
            }
        };
    }

    private static DetectedSlot classifySlot(String key, JsonElement element) {
        String lk = key.toLowerCase();

        boolean isOutput = lk.contains("output") || lk.contains("result");
        boolean isChemical = isChemicalElement(element) || lk.contains("gas") || lk.contains("chemical") || lk.contains("slurry") || lk.contains("infus");
        boolean isFluid = isFluidElement(element) || lk.contains("fluid");

        RecipeForm.Role role;
        if (isOutput) {
            role = (isChemical || isFluid) ? RecipeForm.Role.FLUID_OUT : RecipeForm.Role.OUTPUT;
        } else {
            role = (isChemical || isFluid) ? RecipeForm.Role.FLUID_IN : RecipeForm.Role.INPUT;
        }

        String label = formatLabel(key);
        String amtKey = (isChemical || isFluid) ? (key + "Amount") : null;
        int defAmt = (isChemical || isFluid) ? 1000 : 1;

        return new DetectedSlot(role, label, key, amtKey, defAmt, isOutput);
    }

    private static boolean isChemicalElement(JsonElement elem) {
        if (!elem.isJsonObject()) return false;
        JsonObject obj = elem.getAsJsonObject();
        return obj.has("gas") || obj.has("slurry") || obj.has("infuse_type") || obj.has("gasTag");
    }

    private static boolean isFluidElement(JsonElement elem) {
        if (!elem.isJsonObject()) return false;
        JsonObject obj = elem.getAsJsonObject();
        return obj.has("fluid") || obj.has("FluidName") || obj.has("fluidStack") || obj.has("fluidTag");
    }

    private static String formatLabel(String text) {
        String[] parts = text.split("(?=[A-Z])|[_-]");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isBlank()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private record DetectedSlot(RecipeForm.Role role, String label, String keyPath, String amtKey, int defAmt, boolean isOutput) {}
}
