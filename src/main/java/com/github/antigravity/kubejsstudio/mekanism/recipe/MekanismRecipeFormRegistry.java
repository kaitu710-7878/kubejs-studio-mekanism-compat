package com.github.antigravity.kubejsstudio.mekanism.recipe;

import dev.tysontheember.kubejsstudio.core.op.RecipeForm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and factory for Mekanism, Astral Mekanism, and universal custom machine RecipeForms.
 */
public final class MekanismRecipeFormRegistry {
    private static final Map<String, UniversalMekanismRecipeForm> BUILTIN_FORMS = new LinkedHashMap<>();
    private static final Map<String, UniversalMekanismRecipeForm> DYNAMIC_CACHE = new ConcurrentHashMap<>();

    static {
        registerBuiltins();
    }

    private MekanismRecipeFormRegistry() {}

    public static boolean supports(String typeId) {
        if (typeId == null) return false;
        if (BUILTIN_FORMS.containsKey(typeId) || DYNAMIC_CACHE.containsKey(typeId)) return true;

        String ns = typeId.contains(":") ? typeId.substring(0, typeId.indexOf(':')) : "";
        if ("mekanism".equals(ns) || "astral_mekanism".equals(ns) || "advanced_ae".equals(ns) || "mekanism_extras".equals(ns) || "evolvedmekanism".equals(ns)) {
            return true;
        }
        return false;
    }

    public static UniversalMekanismRecipeForm getOrBuildForm(String typeId) {
        if (typeId == null) return null;
        if (BUILTIN_FORMS.containsKey(typeId)) {
            return BUILTIN_FORMS.get(typeId);
        }
        return DYNAMIC_CACHE.computeIfAbsent(typeId, MekanismRecipeFormRegistry::buildUniversalForm);
    }

    public static List<UniversalMekanismRecipeForm> allBuiltins() {
        return Collections.unmodifiableList(new ArrayList<>(BUILTIN_FORMS.values()));
    }

    private static void registerBuiltins() {
        // 1. Purifying
        registerForm("mekanism:purifying", "Mekanism: Purifying",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Gas Input", 0, 1, "chemicalInput", "gasAmount", 1),
                        slot(RecipeForm.Role.OUTPUT, "Output", 2, 0, "output", null, 1)
                ), List.of());

        // 2. Injecting
        registerForm("mekanism:injecting", "Mekanism: Injecting",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Gas Input", 0, 1, "chemicalInput", "gasAmount", 1),
                        slot(RecipeForm.Role.OUTPUT, "Output", 2, 0, "output", null, 1)
                ), List.of());

        // 3. Compressing
        registerForm("mekanism:compressing", "Mekanism: Compressing",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Gas Input", 0, 1, "chemicalInput", "gasAmount", 1),
                        slot(RecipeForm.Role.OUTPUT, "Output", 2, 0, "output", null, 1)
                ), List.of());

        // 4. Metallurgic Infusing
        registerForm("mekanism:metallurgic_infusing", "Mekanism: Metallurgic Infusing",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Infusion Input", 0, 1, "chemicalInput", "infusionAmount", 10),
                        slot(RecipeForm.Role.OUTPUT, "Output", 2, 0, "output", null, 1)
                ), List.of());

        // 5. Chemical Dissolution Chamber
        registerForm("mekanism:dissolution", "Mekanism: Dissolution",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Gas Input", 0, 1, "gasInput", "gasAmount", 1),
                        slot(RecipeForm.Role.FLUID_OUT, "Slurry Output", 2, 0, "output", "slurryAmount", 1000)
                ), List.of());

        // 6. Chemical Crystallizer
        registerForm("mekanism:crystallizing", "Mekanism: Crystallizing",
                List.of(
                        slot(RecipeForm.Role.FLUID_IN, "Chemical Input", 0, 0, "input", "chemicalAmount", 100),
                        slot(RecipeForm.Role.OUTPUT, "Item Output", 2, 0, "output", null, 1)
                ), List.of(new RecipeForm.Field("chemicalType", "Chemical Type", RecipeForm.Field.Kind.TEXT, true)));

        // 7. Chemical Infuser
        registerForm("mekanism:chemical_infusing", "Mekanism: Chemical Infusing",
                List.of(
                        slot(RecipeForm.Role.FLUID_IN, "Left Gas Input", 0, 0, "leftInput", "leftAmount", 1),
                        slot(RecipeForm.Role.FLUID_IN, "Right Gas Input", 0, 1, "rightInput", "rightAmount", 1),
                        slot(RecipeForm.Role.FLUID_OUT, "Gas Output", 2, 0, "output", "outputAmount", 1)
                ), List.of());

        // 8. Pressurized Reaction Chamber (PRC)
        registerForm("mekanism:reaction", "Mekanism: Reaction Chamber",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Fluid Input", 0, 1, "fluidInput", "fluidAmount", 100),
                        slot(RecipeForm.Role.FLUID_IN, "Gas Input", 0, 2, "gasInput", "gasAmount", 100),
                        slot(RecipeForm.Role.OUTPUT, "Item Output", 2, 0, "itemOutput", null, 1),
                        slot(RecipeForm.Role.FLUID_OUT, "Gas Output", 2, 1, "gasOutput", "gasOutAmount", 100)
                ), List.of(new RecipeForm.Field("duration", "Duration (ticks)", RecipeForm.Field.Kind.INT, false)));

        // 9. Rotary Condensentrator
        registerForm("mekanism:rotary", "Mekanism: Rotary Condensentrator",
                List.of(
                        slot(RecipeForm.Role.FLUID_IN, "Fluid Input", 0, 0, "fluidInput", "fluidAmount", 1),
                        slot(RecipeForm.Role.FLUID_OUT, "Gas Output", 2, 0, "gasOutput", "gasAmount", 1)
                ), List.of());

        // 10. Chemical Oxidizer
        registerForm("mekanism:oxidizing", "Mekanism: Chemical Oxidizer",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "input", null, 1),
                        slot(RecipeForm.Role.FLUID_OUT, "Gas Output", 2, 0, "output", "outputAmount", 100)
                ), List.of());

        // 11. Chemical Washer
        registerForm("mekanism:washing", "Mekanism: Chemical Washer",
                List.of(
                        slot(RecipeForm.Role.FLUID_IN, "Fluid Input", 0, 0, "fluidInput", "fluidAmount", 5),
                        slot(RecipeForm.Role.FLUID_IN, "Slurry Input", 0, 1, "slurryInput", "slurryAmount", 1),
                        slot(RecipeForm.Role.FLUID_OUT, "Clean Slurry Output", 2, 0, "output", "outputAmount", 1)
                ), List.of());

        // 12. Electrolytic Separator
        registerForm("mekanism:separating", "Mekanism: Electrolytic Separator",
                List.of(
                        slot(RecipeForm.Role.FLUID_IN, "Fluid Input", 0, 0, "input", "fluidAmount", 2),
                        slot(RecipeForm.Role.FLUID_OUT, "Left Gas Output", 2, 0, "leftGasOutput", "leftAmount", 2),
                        slot(RecipeForm.Role.FLUID_OUT, "Right Gas Output", 2, 1, "rightGasOutput", "rightAmount", 1)
                ), List.of());

        // 13. Thermal Evaporation Plant
        registerForm("mekanism:evaporating", "Mekanism: Thermal Evaporation",
                List.of(
                        slot(RecipeForm.Role.FLUID_IN, "Fluid Input", 0, 0, "input", "inputAmount", 10),
                        slot(RecipeForm.Role.FLUID_OUT, "Fluid Output", 2, 0, "output", "outputAmount", 1)
                ), List.of());

        // 14. Antiprotonic Nucleosynthesizer
        registerForm("mekanism:nucleosynthesizing", "Mekanism: Nucleosynthesizer",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Gas Input", 0, 1, "gasInput", "gasAmount", 1),
                        slot(RecipeForm.Role.OUTPUT, "Item Output", 2, 0, "output", null, 1)
                ), List.of(new RecipeForm.Field("duration", "Duration (ticks)", RecipeForm.Field.Kind.INT, false)));

        // 15. Astral Mekanism: AME Gas Conversion
        registerForm("astral_mekanism:ame_gas_conversion", "Astral: Gas Conversion",
                List.of(
                        slot(RecipeForm.Role.FLUID_IN, "Input Gas", 0, 0, "input", "inputAmount", 1),
                        slot(RecipeForm.Role.FLUID_OUT, "Output Gas", 2, 0, "output", "outputAmount", 1)
                ), List.of());

        // 16. Advanced AE: Reaction
        registerForm("advanced_ae:reaction", "Advanced AE: Reaction",
                List.of(
                        slot(RecipeForm.Role.INPUT, "Base Item Input", 0, 0, "input_items", null, 1),
                        slot(RecipeForm.Role.FLUID_IN, "Fluid Input", 0, 1, "fluid", null, 1000),
                        slot(RecipeForm.Role.OUTPUT, "Item Output", 2, 0, "output", null, 1)
                ), List.of(new RecipeForm.Field("energy", "Energy (FE)", RecipeForm.Field.Kind.INT, true)));

        // 17. Astral / Evolved Mekanism: Chemixing
        List<UniversalMekanismRecipeForm.SlotDef> chemixingSlots = List.of(
                slot(RecipeForm.Role.INPUT, "Main Item Input", 0, 0, "mainInput", null, 1),
                slot(RecipeForm.Role.INPUT, "Extra Item Input", 0, 1, "extraInput", null, 1),
                slot(RecipeForm.Role.FLUID_IN, "Gas Input", 0, 2, "gasInput", "gasAmount", 50),
                slot(RecipeForm.Role.OUTPUT, "Item Output", 2, 0, "output", null, 1)
        );
        registerForm("evolvedmekanism:chemixing", "Evolved: Chemixing", chemixingSlots, List.of());
        registerForm("astral_mekanism:chemixing", "Astral: Chemixing", chemixingSlots, List.of());
    }

    private static void registerForm(String typeId, String label, List<UniversalMekanismRecipeForm.SlotDef> slots, List<RecipeForm.Field> fields) {
        BUILTIN_FORMS.put(typeId, new UniversalMekanismRecipeForm(typeId, label, slots, fields));
    }

    private static UniversalMekanismRecipeForm.SlotDef slot(RecipeForm.Role role, String label, int col, int row, String jsonPath, String amtKey, int defAmt) {
        return new UniversalMekanismRecipeForm.SlotDef(new RecipeForm.Slot(role, label, col, row), jsonPath, amtKey, defAmt);
    }

    private static UniversalMekanismRecipeForm buildUniversalForm(String typeId) {
        // Automatically construct universal 2-in 2-out layout for any unknown addon or custom machine
        List<UniversalMekanismRecipeForm.SlotDef> slots = List.of(
                slot(RecipeForm.Role.INPUT, "Item Input", 0, 0, "itemInput", null, 1),
                slot(RecipeForm.Role.FLUID_IN, "Chemical/Gas Input", 0, 1, "chemicalInput", "chemicalAmount", 1),
                slot(RecipeForm.Role.OUTPUT, "Item Output", 2, 0, "output", null, 1),
                slot(RecipeForm.Role.FLUID_OUT, "Chemical/Gas Output", 2, 1, "gasOutput", "outputAmount", 1)
        );
        List<RecipeForm.Field> fields = List.of(
                new RecipeForm.Field("duration", "Duration", RecipeForm.Field.Kind.INT, true),
                new RecipeForm.Field("energy", "Energy", RecipeForm.Field.Kind.INT, true)
        );
        return new UniversalMekanismRecipeForm(typeId, null, slots, fields);
    }
}
