package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal registry holding gas-capable recipe definitions and dynamically resolved forms
 * for standard Mekanism machines, addon machines, and KubeJS-created custom machines.
 */
public final class MekanismGasRecipeRegistry {
    private static final Map<MekanismGasRecipeType, RecipeSchemaInfo> BUILTIN_SCHEMAS = new EnumMap<>(MekanismGasRecipeType.class);
    private static final Map<String, GenericGasRecipeForm> DYNAMIC_FORMS = new ConcurrentHashMap<>();

    public record SlotDesc(String role, String label, int col, int row) {}
    public record FieldDesc(String key, String label, String type, String defaultValue) {}

    public static final class RecipeSchemaInfo {
        private final MekanismGasRecipeType type;
        private final List<SlotDesc> slots;
        private final List<FieldDesc> fields;

        public RecipeSchemaInfo(MekanismGasRecipeType type, List<SlotDesc> slots, List<FieldDesc> fields) {
            this.type = type;
            this.slots = List.copyOf(slots);
            this.fields = List.copyOf(fields);
        }

        public MekanismGasRecipeType type() { return type; }
        public List<SlotDesc> slots() { return slots; }
        public List<FieldDesc> fields() { return fields; }
    }

    public static void init() {
        registerBuiltins();
    }

    /**
     * Resolves or dynamically generates a recipe form for any recipe type.
     * If the recipe is from an addon or a custom KubeJS machine, it uses introspection to create a form.
     */
    public static GenericGasRecipeForm resolveForm(JsonObject recipeJson) {
        String typeId = recipeJson.has("type") ? recipeJson.get("type").getAsString() : "";
        if (typeId.isBlank()) return null;

        // Check cache
        GenericGasRecipeForm cached = DYNAMIC_FORMS.get(typeId);
        if (cached != null) return cached;

        // Run universal introspection
        GasRecipeIntrospector.IntrospectionResult result = GasRecipeIntrospector.inspect(recipeJson);
        GenericGasRecipeForm form = GenericGasRecipeForm.fromIntrospection(result);
        DYNAMIC_FORMS.put(typeId, form);
        return form;
    }

    /**
     * Manually registers a dynamic gas recipe form (e.g. from an addon or custom script).
     */
    public static void registerDynamicForm(GenericGasRecipeForm form) {
        if (form != null) {
            DYNAMIC_FORMS.put(form.typeId(), form);
        }
    }

    public static GenericGasRecipeForm getDynamicForm(String typeId) {
        return DYNAMIC_FORMS.get(typeId);
    }

    public static Map<String, GenericGasRecipeForm> getAllDynamicForms() {
        return Collections.unmodifiableMap(DYNAMIC_FORMS);
    }

    private static void registerBuiltins() {
        // Built-in standard Mekanism schemas (convenience defaults)
        registerBuiltin(MekanismGasRecipeType.CRYSTALLIZING,
                List.of(
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 0),
                        new SlotDesc("ITEM_OUTPUT", "kubejsstudio.recipes.slot.output", 2, 0)
                ),
                List.of(
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "200"),
                        new FieldDesc("itemOutputCount", "kubejsstudio.mekanism.output_count", "INT", "1")
                )
        );

        registerBuiltin(MekanismGasRecipeType.OXIDIZING,
                List.of(
                        new SlotDesc("ITEM_INPUT", "kubejsstudio.recipes.slot.input", 0, 0),
                        new SlotDesc("GAS_OUTPUT", "kubejsstudio.mekanism.slot.gas_output", 2, 0)
                ),
                List.of(
                        new FieldDesc("itemInputCount", "kubejsstudio.mekanism.input_count", "INT", "1"),
                        new FieldDesc("gasOutputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "100")
                )
        );

        registerBuiltin(MekanismGasRecipeType.PURIFYING,
                List.of(
                        new SlotDesc("ITEM_INPUT", "kubejsstudio.recipes.slot.input", 0, 0),
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 1),
                        new SlotDesc("ITEM_OUTPUT", "kubejsstudio.recipes.slot.output", 2, 0)
                ),
                List.of(
                        new FieldDesc("itemInputCount", "kubejsstudio.mekanism.input_count", "INT", "1"),
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "1"),
                        new FieldDesc("itemOutputCount", "kubejsstudio.mekanism.output_count", "INT", "1")
                )
        );

        registerBuiltin(MekanismGasRecipeType.INJECTING,
                List.of(
                        new SlotDesc("ITEM_INPUT", "kubejsstudio.recipes.slot.input", 0, 0),
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 1),
                        new SlotDesc("ITEM_OUTPUT", "kubejsstudio.recipes.slot.output", 2, 0)
                ),
                List.of(
                        new FieldDesc("itemInputCount", "kubejsstudio.mekanism.input_count", "INT", "1"),
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "1"),
                        new FieldDesc("itemOutputCount", "kubejsstudio.mekanism.output_count", "INT", "1")
                )
        );

        registerBuiltin(MekanismGasRecipeType.COMPRESSING,
                List.of(
                        new SlotDesc("ITEM_INPUT", "kubejsstudio.recipes.slot.input", 0, 0),
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 1),
                        new SlotDesc("ITEM_OUTPUT", "kubejsstudio.recipes.slot.output", 2, 0)
                ),
                List.of(
                        new FieldDesc("itemInputCount", "kubejsstudio.mekanism.input_count", "INT", "1"),
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "1"),
                        new FieldDesc("itemOutputCount", "kubejsstudio.mekanism.output_count", "INT", "1")
                )
        );

        registerBuiltin(MekanismGasRecipeType.DISSOLUTION,
                List.of(
                        new SlotDesc("ITEM_INPUT", "kubejsstudio.recipes.slot.input", 0, 0),
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 1),
                        new SlotDesc("GAS_OUTPUT", "kubejsstudio.mekanism.slot.gas_output", 2, 0)
                ),
                List.of(
                        new FieldDesc("itemInputCount", "kubejsstudio.mekanism.input_count", "INT", "1"),
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "100"),
                        new FieldDesc("gasOutputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "1000")
                )
        );

        registerBuiltin(MekanismGasRecipeType.ROTARY,
                List.of(
                        new SlotDesc("FLUID_INPUT", "kubejsstudio.recipes.slot.fluid_input", 0, 0),
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 1),
                        new SlotDesc("FLUID_OUTPUT", "kubejsstudio.recipes.slot.fluid_output", 2, 0),
                        new SlotDesc("GAS_OUTPUT", "kubejsstudio.mekanism.slot.gas_output", 2, 1)
                ),
                List.of(
                        new FieldDesc("fluidInputAmount", "kubejsstudio.mekanism.fluid_amount", "INT", "1"),
                        new FieldDesc("gasOutputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "1"),
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "1"),
                        new FieldDesc("fluidOutputAmount", "kubejsstudio.mekanism.fluid_amount", "INT", "1")
                )
        );

        registerBuiltin(MekanismGasRecipeType.REACTION,
                List.of(
                        new SlotDesc("ITEM_INPUT", "kubejsstudio.recipes.slot.input", 0, 0),
                        new SlotDesc("FLUID_INPUT", "kubejsstudio.recipes.slot.fluid_input", 0, 1),
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 2),
                        new SlotDesc("ITEM_OUTPUT", "kubejsstudio.recipes.slot.output", 2, 0),
                        new SlotDesc("GAS_OUTPUT", "kubejsstudio.mekanism.slot.gas_output", 2, 1)
                ),
                List.of(
                        new FieldDesc("itemInputCount", "kubejsstudio.mekanism.input_count", "INT", "1"),
                        new FieldDesc("fluidInputAmount", "kubejsstudio.mekanism.fluid_amount", "INT", "100"),
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "100"),
                        new FieldDesc("itemOutputCount", "kubejsstudio.mekanism.output_count", "INT", "1"),
                        new FieldDesc("gasOutputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "100"),
                        new FieldDesc("duration", "kubejsstudio.mekanism.duration", "INT", "60"),
                        new FieldDesc("energyRequired", "kubejsstudio.mekanism.energy_required", "FLOAT", "1000")
                )
        );
    }

    private static void registerBuiltin(MekanismGasRecipeType type, List<SlotDesc> slots, List<FieldDesc> fields) {
        BUILTIN_SCHEMAS.put(type, new RecipeSchemaInfo(type, slots, fields));
    }

    public static RecipeSchemaInfo getSchema(MekanismGasRecipeType type) {
        return BUILTIN_SCHEMAS.get(type);
    }
}
