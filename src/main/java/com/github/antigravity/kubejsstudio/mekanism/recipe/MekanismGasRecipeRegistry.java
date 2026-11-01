package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry holding gas-capable recipe definitions and schema mappings for KubeJS Studio.
 */
public final class MekanismGasRecipeRegistry {
    private static final Map<MekanismGasRecipeType, RecipeSchemaInfo> SCHEMAS = new EnumMap<>(MekanismGasRecipeType.class);

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
        // Crystallizing: Gas Input (col 0, row 0) -> Item Output (col 2, row 0)
        register(MekanismGasRecipeType.CRYSTALLIZING,
                List.of(
                        new SlotDesc("GAS_INPUT", "kubejsstudio.mekanism.slot.gas_input", 0, 0),
                        new SlotDesc("ITEM_OUTPUT", "kubejsstudio.recipes.slot.output", 2, 0)
                ),
                List.of(
                        new FieldDesc("gasInputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "200"),
                        new FieldDesc("itemOutputCount", "kubejsstudio.mekanism.output_count", "INT", "1")
                )
        );

        // Oxidizing: Item Input (col 0, row 0) -> Gas Output (col 2, row 0)
        register(MekanismGasRecipeType.OXIDIZING,
                List.of(
                        new SlotDesc("ITEM_INPUT", "kubejsstudio.recipes.slot.input", 0, 0),
                        new SlotDesc("GAS_OUTPUT", "kubejsstudio.mekanism.slot.gas_output", 2, 0)
                ),
                List.of(
                        new FieldDesc("itemInputCount", "kubejsstudio.mekanism.input_count", "INT", "1"),
                        new FieldDesc("gasOutputAmount", "kubejsstudio.mekanism.gas_amount", "INT", "100")
                )
        );

        // Purifying: Item Input (col 0, row 0), Gas Input (col 0, row 1) -> Item Output (col 2, row 0)
        register(MekanismGasRecipeType.PURIFYING,
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

        // Injecting
        register(MekanismGasRecipeType.INJECTING,
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

        // Compressing
        register(MekanismGasRecipeType.COMPRESSING,
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

        // Dissolution: Item Input (col 0, row 0), Gas Input (col 0, row 1) -> Gas Output (col 2, row 0)
        register(MekanismGasRecipeType.DISSOLUTION,
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

        // Rotary Condensentrator: Fluid / Gas conversion
        register(MekanismGasRecipeType.ROTARY,
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

        // Reaction (PRC): Item Input, Fluid Input, Gas Input -> Item Output, Gas Output
        register(MekanismGasRecipeType.REACTION,
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

    private static void register(MekanismGasRecipeType type, List<SlotDesc> slots, List<FieldDesc> fields) {
        SCHEMAS.put(type, new RecipeSchemaInfo(type, slots, fields));
    }

    public static RecipeSchemaInfo getSchema(MekanismGasRecipeType type) {
        return SCHEMAS.get(type);
    }

    public static Map<MekanismGasRecipeType, RecipeSchemaInfo> getAllSchemas() {
        return Collections.unmodifiableMap(SCHEMAS);
    }
}
