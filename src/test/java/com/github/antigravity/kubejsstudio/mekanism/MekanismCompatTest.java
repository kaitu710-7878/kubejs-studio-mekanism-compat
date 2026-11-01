package com.github.antigravity.kubejsstudio.mekanism;

import com.github.antigravity.kubejsstudio.mekanism.gas.GasScriptSerializer;
import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;
import com.github.antigravity.kubejsstudio.mekanism.recipe.GasRecipeIntrospector;
import com.github.antigravity.kubejsstudio.mekanism.recipe.GenericGasRecipeForm;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeDefinition;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeRegistry;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeType;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasStack;
import com.github.antigravity.kubejsstudio.mekanism.tag.GasTagSerializer;
import com.github.antigravity.kubejsstudio.mekanism.tag.MekanismGasTagDefinition;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MekanismCompatTest {

    @BeforeAll
    public static void setUp() {
        MekanismGasRecipeRegistry.init();
    }

    @Test
    public void testGasDefinitionAndSerialization() {
        MekanismGasDefinition gas = MekanismGasDefinition.builder("kubejs:super_gas")
                .displayName("Super Gas")
                .color(0x33FF99)
                .radioactivity(0.5)
                .tag("forge:gases/clean_iron")
                .build();

        assertEquals("kubejs:super_gas", gas.id());
        assertEquals("Super Gas", gas.displayName());
        assertEquals(0x33FF99, gas.color());
        assertEquals(0.5, gas.radioactivity());
        assertTrue(gas.tags().contains("forge:gases/clean_iron"));

        String script = GasScriptSerializer.buildStartupScript(List.of(gas));
        assertTrue(script.contains("StartupEvents.registry('mekanism:gas'"));
        assertTrue(script.contains("event.create('super_gas')"));
        assertTrue(script.contains(".displayName('Super Gas')"));
        assertTrue(script.contains(".color(0x0033FF99)") || script.contains(".color(0x33FF99)"));
        assertTrue(script.contains(".radioactivity(0.5)"));
        assertTrue(script.contains(".tag('forge:gases/clean_iron')"));

        // Round-trip parse test
        List<MekanismGasDefinition> parsed = GasScriptSerializer.parseScript(script);
        assertEquals(1, parsed.size());
        assertEquals("kubejs:super_gas", parsed.get(0).id());
        assertEquals("Super Gas", parsed.get(0).displayName());
        assertEquals(0x33FF99, parsed.get(0).color());
        assertEquals(0.5, parsed.get(0).radioactivity());
        assertTrue(parsed.get(0).tags().contains("forge:gases/clean_iron"));
    }

    @Test
    public void testGasTagDefinitionAndSerialization() {
        MekanismGasTagDefinition tag = new MekanismGasTagDefinition(
                "forge:gases/clean_iron",
                List.of("kubejs:super_gas"),
                List.of("mekanism:hydrogen"),
                false
        );

        String script = GasTagSerializer.buildServerScript(List.of(tag));
        assertTrue(script.contains("ServerEvents.tags('mekanism:gas'"));
        assertTrue(script.contains("event.add('forge:gases/clean_iron', 'kubejs:super_gas');"));
        assertTrue(script.contains("event.remove('forge:gases/clean_iron', 'mekanism:hydrogen');"));

        String json = tag.toDatapackJson();
        assertTrue(json.contains("\"replace\": false"));
        assertTrue(json.contains("kubejs:super_gas"));
        assertTrue(json.contains("mekanism:hydrogen"));
    }

    @Test
    public void testOxidizingRecipe() {
        MekanismGasRecipeDefinition recipe = new MekanismGasRecipeDefinition(
                MekanismGasRecipeType.OXIDIZING,
                "minecraft:redstone", 1,
                null,
                null, 0,
                null, 0,
                new MekanismGasStack("mekanism:oxygen", 100),
                null, 0,
                0, 0
        );

        String script = recipe.toKubeJsScript();
        assertEquals("event.recipes.mekanism.oxidizing('100x mekanism:oxygen', 'minecraft:redstone');", script);

        String json = recipe.toJson();
        assertTrue(json.contains("\"type\": \"mekanism:oxidizing\""));
        assertTrue(json.contains("\"gas\": \"mekanism:oxygen\""));
        assertTrue(json.contains("\"amount\": 100"));
    }

    @Test
    public void testPurifyingRecipe() {
        MekanismGasRecipeDefinition recipe = new MekanismGasRecipeDefinition(
                MekanismGasRecipeType.PURIFYING,
                "#forge:ores/iron", 1,
                new MekanismGasStack("mekanism:oxygen", 1),
                null, 0,
                "mekanism:purified_iron", 2,
                null,
                null, 0,
                0, 0
        );

        String script = recipe.toKubeJsScript();
        assertEquals("event.recipes.mekanism.purifying('2x mekanism:purified_iron', '#forge:ores/iron', '1x mekanism:oxygen');", script);

        String json = recipe.toJson();
        assertTrue(json.contains("\"type\": \"mekanism:purifying\""));
        assertTrue(json.contains("\"tag\": \"forge:ores/iron\""));
        assertTrue(json.contains("\"gas\": \"mekanism:oxygen\""));
        assertTrue(json.contains("\"item\": \"mekanism:purified_iron\""));
        assertTrue(json.contains("\"count\": 2"));
    }

    @Test
    public void testCrystallizingRecipe() {
        MekanismGasRecipeDefinition recipe = new MekanismGasRecipeDefinition(
                MekanismGasRecipeType.CRYSTALLIZING,
                null, 0,
                new MekanismGasStack("kubejs:super_gas", 200),
                null, 0,
                "kubejs:super_crystal", 1,
                null,
                null, 0,
                0, 0
        );

        String script = recipe.toKubeJsScript();
        assertEquals("event.recipes.mekanism.crystallizing('kubejs:super_crystal', '200x kubejs:super_gas');", script);

        String json = recipe.toJson();
        assertTrue(json.contains("\"type\": \"mekanism:crystallizing\""));
        assertTrue(json.contains("\"gas\": \"kubejs:super_gas\""));
        assertTrue(json.contains("\"item\": \"kubejs:super_crystal\""));
    }

    /**
     * Test universal auto-detection for an ADDON machine recipe.
     * e.g. An addon providing an advanced separator machine that converts item + gas into gas output.
     */
    @Test
    public void testUniversalAddonMachineAutoDetection() {
        String addonRecipeJson = """
        {
          "type": "mekanism_addon:chemical_separator",
          "itemInput": { "item": "minecraft:iron_ingot" },
          "gasInput": { "gas": "mekanism:oxygen", "amount": 250 },
          "gasOutput": { "gas": "kubejs:refined_steam", "amount": 500 },
          "energyCost": 1500,
          "duration": 40
        }
        """;

        JsonObject json = JsonParser.parseString(addonRecipeJson).getAsJsonObject();
        GenericGasRecipeForm form = MekanismGasRecipeRegistry.resolveForm(json);

        assertNotNull(form);
        assertEquals("mekanism_addon:chemical_separator", form.typeId());
        assertTrue(form.slots().size() >= 3, "Should detect itemInput, gasInput, and gasOutput slots");

        // Verify universal script generation
        String script = form.toKubeJsScript(
                List.of("minecraft:iron_ingot", "mekanism:oxygen", "kubejs:refined_steam"),
                Map.of("energyCost", "1500", "duration", "40", "gasInput.amount", "250", "gasOutput.amount", "500")
        );

        assertTrue(script.contains("event.custom("));
        assertTrue(script.contains("mekanism_addon:chemical_separator"));
        assertTrue(script.contains("refined_steam"));
    }

    /**
     * Test universal auto-detection for a custom KubeJS-created machine recipe.
     */
    @Test
    public void testUniversalKubeJsCustomMachineAutoDetection() {
        String customKubeMachineJson = """
        {
          "type": "kubejs:custom_gas_refinery",
          "input": { "item": "minecraft:gold_ore" },
          "chemicalInput": { "gas": "kubejs:super_gas", "amount": 50 },
          "output": { "item": "minecraft:gold_ingot", "count": 3 },
          "chemicalOutput": { "gas": "mekanism:hydrogen", "amount": 100 },
          "processingTicks": 100
        }
        """;

        JsonObject json = JsonParser.parseString(customKubeMachineJson).getAsJsonObject();
        GenericGasRecipeForm form = MekanismGasRecipeRegistry.resolveForm(json);

        assertNotNull(form);
        assertEquals("kubejs:custom_gas_refinery", form.typeId());
        assertEquals("Custom Gas Refinery", form.label());

        String generatedJson = form.toJson(
                List.of("minecraft:gold_ore", "kubejs:super_gas", "minecraft:gold_ingot", "mekanism:hydrogen"),
                Map.of("chemicalInput.amount", "50", "chemicalOutput.amount", "100", "processingTicks", "100")
        );

        assertTrue(generatedJson.contains("\"type\": \"kubejs:custom_gas_refinery\""));
        assertTrue(generatedJson.contains("gold_ore"));
        assertTrue(generatedJson.contains("super_gas"));
        assertTrue(generatedJson.contains("gold_ingot"));
        assertTrue(generatedJson.contains("hydrogen"));
    }

    @Test
    public void testGasCreateUiWorkflow() {
        com.github.antigravity.kubejsstudio.mekanism.client.GasCreateUi ui = new com.github.antigravity.kubejsstudio.mekanism.client.GasCreateUi();
        ui.startNew();
        ui.setCurrentNamespace("kubejs");
        ui.setCurrentPath("plasma_gas");
        ui.setCurrentDisplayName("Plasma Gas");
        ui.setCurrentColor(0xFF0055);
        ui.setCurrentRadioactivity(1.2);
        ui.setTagsFromString("forge:gases/hot, forge:gases/energy");

        boolean ok = ui.queueCurrent();
        assertTrue(ok, "Should successfully queue custom gas");
        assertTrue(ui.pendingGases().containsKey("kubejs:plasma_gas"));

        MekanismGasDefinition queued = ui.pendingGases().get("kubejs:plasma_gas");
        assertEquals("Plasma Gas", queued.displayName());
        assertEquals(0xFF0055, queued.color());
        assertEquals(1.2, queued.radioactivity());
        assertEquals(2, queued.tags().size());
        assertTrue(queued.tags().contains("forge:gases/hot"));
    }
}
