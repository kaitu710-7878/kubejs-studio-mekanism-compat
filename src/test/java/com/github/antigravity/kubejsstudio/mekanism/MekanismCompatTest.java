package com.github.antigravity.kubejsstudio.mekanism;

import com.github.antigravity.kubejsstudio.mekanism.gas.GasScriptSerializer;
import com.github.antigravity.kubejsstudio.mekanism.gas.MekanismGasDefinition;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeDefinition;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeRegistry;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasRecipeType;
import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismGasStack;
import com.github.antigravity.kubejsstudio.mekanism.tag.GasTagSerializer;
import com.github.antigravity.kubejsstudio.mekanism.tag.MekanismGasTagDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

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
}
