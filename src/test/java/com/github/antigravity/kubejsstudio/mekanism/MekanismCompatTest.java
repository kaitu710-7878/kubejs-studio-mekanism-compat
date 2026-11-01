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

    @Test
    public void testUniversalMekanismRecipeFormPurifying() {
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("mekanism:purifying");
        assertNotNull(form);

        String sampleJson = """
        {
          "type": "mekanism:purifying",
          "itemInput": {
            "ingredient": {
              "tag": "forge:ores/iron"
            }
          },
          "chemicalInput": {
            "amount": 1,
            "gas": "mekanism:oxygen"
          },
          "output": {
            "item": "mekanism:clump_iron",
            "count": 3
          }
        }
        """;

        JsonObject json = JsonParser.parseString(sampleJson).getAsJsonObject();
        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertNotNull(parsed);
        assertEquals("#forge:ores/iron", parsed.slots().get(0));
        assertEquals("mekanism:oxygen", parsed.slots().get(1));
        assertEquals("mekanism:clump_iron", parsed.slots().get(2));
    }

    @Test
    public void testUniversalMekanismRecipeFormChemixing() {
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("evolvedmekanism:chemixing");
        assertNotNull(form);

        String chemixingJson = """
        {
            "type": "evolvedmekanism:chemixing",
            "extraInput": {
                "ingredient": {
                    "item": "astral_mekanism:alloy_stardust"
                }
            },
            "gasInput": {
                "amount": 50,
                "gas": "astral_mekanism:astral_ether"
            },
            "mainInput": {
                "ingredient": {
                    "item": "astral_mekanism:mekanical_composter"
                }
            },
            "output": {
                "item": "astral_mekanism:astral_composter"
            }
        }
        """;

        JsonObject json = JsonParser.parseString(chemixingJson).getAsJsonObject();
        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertNotNull(parsed);
        assertEquals("astral_mekanism:mekanical_composter", parsed.slots().get(0));
        assertEquals("astral_mekanism:alloy_stardust", parsed.slots().get(1));
        assertEquals("astral_mekanism:astral_ether", parsed.slots().get(2));
        assertEquals("astral_mekanism:astral_composter", parsed.slots().get(3));
        assertEquals("50", parsed.fields().get("gasAmount"));

        String outputJson = form.toJson(parsed.slots(), parsed.fields());
        assertTrue(outputJson.contains("astral_mekanism:mekanical_composter"));
        assertTrue(outputJson.contains("astral_mekanism:alloy_stardust"));
        assertTrue(outputJson.contains("astral_mekanism:astral_ether"));
        assertTrue(outputJson.contains("astral_mekanism:astral_composter"));
    }

    @Test
    public void testMekanismRegistryLookupValidation() {
        assertTrue(com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRegistryLookup.isValidChemicalId("mekanism:oxygen"));
        assertTrue(com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRegistryLookup.isValidChemicalId("astral_mekanism:astral_ether"));
        assertTrue(com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRegistryLookup.isValidChemicalTag("forge:gases/clean_iron"));
        assertTrue(com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRegistryLookup.isValidChemicalTag("#mekanism:clean_slurry"));
    }

    @Test
    public void testAstralReconstructionRecipe() {
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("astral_mekanism:reconstruction");
        assertNotNull(form);

        String sampleJson = """
        {
            "type": "astral_mekanism:reconstruction",
            "duration": 256,
            "energyRequired": 10000000000000,
            "itemNotConsumed": false,
            "fluidInput": {
                "amount": 1000000,
                "fluid": "astral_mekanism:wisdom_rivulet"
            },
            "gasInput": {
                "amount": 100000000,
                "gas": "astral_mekanism:interstellar_antimatter"
            },
            "gasOutput": {
                "amount": 1000000000,
                "gas": "mekanism:lithium"
            },
            "itemInput": {
                "amount": 4,
                "ingredient": {
                    "item": "astral_mekanism:coodinate_alloy"
                }
            },
            "itemOutput": {
                "item": "evolvedmekanism:alloy_creative"
            }
        }
        """;

        JsonObject json = JsonParser.parseString(sampleJson).getAsJsonObject();
        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertNotNull(parsed);

        // Verify all 5 slots are accurately extracted
        assertEquals("astral_mekanism:coodinate_alloy", parsed.slots().get(0), "Item Input");
        assertEquals("astral_mekanism:interstellar_antimatter", parsed.slots().get(1), "Gas Input");
        assertEquals("astral_mekanism:wisdom_rivulet", parsed.slots().get(2), "Fluid Input");
        assertEquals("evolvedmekanism:alloy_creative", parsed.slots().get(3), "Item Output");
        assertEquals("mekanism:lithium", parsed.slots().get(4), "Gas Output");
        assertEquals("256", parsed.fields().get("duration"));

        String outputJson = form.toJson(parsed.slots(), parsed.fields());
        assertTrue(outputJson.contains("coodinate_alloy"));
        assertTrue(outputJson.contains("interstellar_antimatter"));
        assertTrue(outputJson.contains("wisdom_rivulet"));
        assertTrue(outputJson.contains("alloy_creative"));
        assertTrue(outputJson.contains("lithium"));
    }

    @Test
    public void testAllChemicalTypesSerialization() {
        MekanismGasDefinition gas = MekanismGasDefinition.builder("kubejs:rad_gas")
                .chemicalType("gas")
                .displayName("Radioactive Gas")
                .radioactivity(2.5)
                .build();
        assertTrue(gas.toKubeJsStatement().contains(".radioactivity(2.5)"));

        MekanismGasDefinition infuse = MekanismGasDefinition.builder("kubejs:super_infuse")
                .chemicalType("infuse_type")
                .displayName("Super Infusion")
                .color(0xFF3300)
                .build();
        assertFalse(infuse.toKubeJsStatement().contains("radioactivity"));

        MekanismGasDefinition pigment = MekanismGasDefinition.builder("kubejs:neon_pigment")
                .chemicalType("pigment")
                .displayName("Neon Pigment")
                .build();

        MekanismGasDefinition slurry = MekanismGasDefinition.builder("kubejs:clean_gold_slurry")
                .chemicalType("slurry")
                .displayName("Clean Gold Slurry")
                .build();

        String startupScript = GasScriptSerializer.buildStartupScript(List.of(gas, infuse, pigment, slurry));
        assertTrue(startupScript.contains("StartupEvents.registry('mekanism:gas'"));
        assertTrue(startupScript.contains("StartupEvents.registry('mekanism:infuse_type'"));
        assertTrue(startupScript.contains("StartupEvents.registry('mekanism:pigment'"));
        assertTrue(startupScript.contains("StartupEvents.registry('mekanism:slurry'"));
    }

    @Test
    public void testEnrichingChamberNoPhantomGasSlots() {
        String enrichingJson = """
        {
          "type": "mekanism:enriching",
          "input": {
            "ingredient": { "item": "minecraft:nether_star" }
          },
          "output": {
            "item": "astral_mekanism:enriched_nether_star"
          }
        }
        """;
        JsonObject json = JsonParser.parseString(enrichingJson).getAsJsonObject();
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("mekanism:enriching", json);
        assertNotNull(form);

        // Crucial test: Enrichment Chamber MUST have exactly 2 slots (1 item in, 1 item out), ZERO gas slots!
        assertEquals(2, form.slots().size(), "Enriching chamber must only have item in and item out slots");
        assertEquals(dev.tysontheember.kubejsstudio.core.op.RecipeForm.Role.INPUT, form.slots().get(0).role());
        assertEquals(dev.tysontheember.kubejsstudio.core.op.RecipeForm.Role.OUTPUT, form.slots().get(1).role());

        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertEquals("minecraft:nether_star", parsed.slots().get(0));
        assertEquals("astral_mekanism:enriched_nether_star", parsed.slots().get(1));
    }

    @Test
    public void testMekanicalTransformAllSlots() {
        String transformJson = """
        {
            "type": "astral_mekanism:mekanical_transform",
            "itemInputA": { "ingredient": { "item": "minecraft:diamond_pickaxe" } },
            "itemInputB": { "ingredient": { "item": "minecraft:soul_soil" }, "amount": 1 },
            "itemInputC": { "ingredient": { "item": "minecraft:blue_ice" }, "amount": 1 },
            "fluidInputA": { "fluid": "minecraft:lava", "amount": 1000 },
            "fluidInputB": { "fluid": "minecraft:water", "amount": 1000 },
            "outputItem": { "item": "minecraft:basalt", "count": 1 }
        }
        """;
        JsonObject json = JsonParser.parseString(transformJson).getAsJsonObject();
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("astral_mekanism:mekanical_transform", json);
        assertNotNull(form);

        // 3 item inputs + 2 fluid inputs + 1 item output = 6 slots
        assertEquals(6, form.slots().size(), "Must have all 3 items, 2 fluids, and 1 item output");

        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertTrue(parsed.slots().contains("minecraft:diamond_pickaxe"));
        assertTrue(parsed.slots().contains("minecraft:soul_soil"));
        assertTrue(parsed.slots().contains("minecraft:blue_ice"));
        assertTrue(parsed.slots().contains("minecraft:lava"));
        assertTrue(parsed.slots().contains("minecraft:water"));
        assertTrue(parsed.slots().contains("minecraft:basalt"));
    }

    @Test
    public void testRefinedAstralEtherInfusingCondense() {
        String etherJson = """
        {
            "type": "astral_mekanism:infusing_condense",
            "inputGas": {
                "amount": 1,
                "gas": "astral_mekanism:astral_ether"
            },
            "inputInfusion": {
                "infuse_type": "astral_mekanism:xp",
                "amount": 100000
            },
            "output": {
                "fluid": "astral_mekanism:refined_astral_ether",
                "amount": 1
            }
        }
        """;
        JsonObject json = JsonParser.parseString(etherJson).getAsJsonObject();
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("astral_mekanism:infusing_condense", json);
        assertNotNull(form);

        // Exactly 3 slots: Gas Input, Infusion Input, Fluid Output. ZERO phantom item slots!
        assertEquals(3, form.slots().size());
        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertEquals("astral_mekanism:astral_ether", parsed.slots().get(0));
        assertEquals("astral_mekanism:xp", parsed.slots().get(1));
        assertEquals("astral_mekanism:refined_astral_ether", parsed.slots().get(2));
    }

    @Test
    public void testAstralCraftingTableAllSlots() {
        String craftingJson = """
        {
            "type": "astral_mekanism:astral_crafting",
            "itemInputA1": { "ingredient": { "item": "astral_mekanism:alloy_elastic" } },
            "itemInputC3": { "ingredient": { "item": "evolvedmekanism:alloyer" } },
            "fluidInput": { "fluid": "minecraft:lava", "amount": 10000 },
            "gasInput": { "gas": "astral_mekanism:astral_ether", "amount": 10000 },
            "output": { "item": "astral_mekanism:astral_alloyer", "count": 1 }
        }
        """;
        JsonObject json = JsonParser.parseString(craftingJson).getAsJsonObject();
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("astral_mekanism:astral_crafting", json);
        assertNotNull(form);

        // Contains grid items + fluid + gas + output
        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertTrue(parsed.slots().contains("astral_mekanism:alloy_elastic"));
        assertTrue(parsed.slots().contains("evolvedmekanism:alloyer"));
        assertTrue(parsed.slots().contains("minecraft:lava"));
        assertTrue(parsed.slots().contains("astral_mekanism:astral_ether"));
        assertTrue(parsed.slots().contains("astral_mekanism:astral_alloyer"));
    }

    @Test
    public void testMekDataJetpackRecipeParsing() {
        String jetpackJson = """
        {
          "type": "mekanism:mek_data",
          "key": {
            "C": { "tag": "forge:circuits/basic" },
            "I": { "tag": "forge:ingots/tin" },
            "S": { "tag": "forge:ingots/steel" },
            "T": { "item": "mekanism:basic_chemical_tank" }
          },
          "pattern": [
            "SCS",
            "ITI",
            " I "
          ],
          "result": {
            "item": "mekanism:jetpack"
          }
        }
        """;
        JsonObject json = JsonParser.parseString(jetpackJson).getAsJsonObject();
        com.github.antigravity.kubejsstudio.mekanism.recipe.MekDataRecipeParser.ParsedMekData parsed =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekDataRecipeParser.parse(json);

        assertNotNull(parsed);
        assertTrue(parsed.isShaped());
        assertEquals("mekanism:jetpack", parsed.result());
        assertEquals(1, parsed.resultCount());

        // Row 0: S, C, S
        assertEquals("#forge:ingots/steel", parsed.grid()[0]);
        assertEquals("#forge:circuits/basic", parsed.grid()[1]);
        assertEquals("#forge:ingots/steel", parsed.grid()[2]);

        // Row 1: I, T, I
        assertEquals("#forge:ingots/tin", parsed.grid()[3]);
        assertEquals("mekanism:basic_chemical_tank", parsed.grid()[4]);
        assertEquals("#forge:ingots/tin", parsed.grid()[5]);

        // Row 2: " ", I, " "
        assertEquals("", parsed.grid()[6]);
        assertEquals("#forge:ingots/tin", parsed.grid()[7]);
        assertEquals("", parsed.grid()[8]);
    }

    @Test
    public void testMekDataLaserRecipeParsing() {
        String laserJson = """
        {
          "type": "mekanism:mek_data",
          "key": {
            "C": { "item": "mekanism:steel_casing" },
            "D": { "tag": "forge:gems/diamond" },
            "O": { "item": "mekanism:energy_tablet" },
            "R": { "tag": "forge:alloys/reinforced" }
          },
          "pattern": [
            "ROR",
            "RDR",
            "RCR"
          ],
          "result": {
            "item": "mekanism:laser"
          }
        }
        """;
        JsonObject json = JsonParser.parseString(laserJson).getAsJsonObject();
        com.github.antigravity.kubejsstudio.mekanism.recipe.MekDataRecipeParser.ParsedMekData parsed =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekDataRecipeParser.parse(json);

        assertNotNull(parsed);
        assertTrue(parsed.isShaped());
        assertEquals("mekanism:laser", parsed.result());

        // Row 0: R, O, R
        assertEquals("#forge:alloys/reinforced", parsed.grid()[0]);
        assertEquals("mekanism:energy_tablet", parsed.grid()[1]);
        assertEquals("#forge:alloys/reinforced", parsed.grid()[2]);

        // Row 1: R, D, R
        assertEquals("#forge:alloys/reinforced", parsed.grid()[3]);
        assertEquals("#forge:gems/diamond", parsed.grid()[4]);
        assertEquals("#forge:alloys/reinforced", parsed.grid()[5]);

        // Row 2: R, C, R
        assertEquals("#forge:alloys/reinforced", parsed.grid()[6]);
        assertEquals("mekanism:steel_casing", parsed.grid()[7]);
        assertEquals("#forge:alloys/reinforced", parsed.grid()[8]);
    }

    @Test
    public void testAstralMekanismReconstructionCreativeAlloyDynamicInspection() {
        String reconstructionJson = """
        {
            "type": "astral_mekanism:reconstruction",
            "duration": 256,
            "energyRequired": 10000000000000,
            "itemNotConsumed": false,
            "fluidInput": {
                "amount": 1000000,
                "fluid": "astral_mekanism:wisdom_rivulet"
            },
            "gasInput": {
                "amount": 100000000,
                "gas": "astral_mekanism:interstellar_antimatter"
            },
            "gasOutput": {
                "amount": 1000000000,
                "gas": "mekanism:lithium"
            },
            "itemInput": {
                "amount": 4,
                "ingredient": {
                    "item": "astral_mekanism:coodinate_alloy"
                }
            },
            "itemOutput": {
                "item": "evolvedmekanism:alloy_creative"
            }
        }
        """;
        JsonObject json = JsonParser.parseString(reconstructionJson).getAsJsonObject();
        com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm form =
                com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry.getOrBuildForm("astral_mekanism:reconstruction", json);

        assertNotNull(form);

        // Exactly 5 slots: 1 Item Input, 1 Fluid Input, 1 Gas Input, 1 Item Output, 1 Gas Output
        assertEquals(5, form.slots().size(), "Must have exactly 5 detected slots for reconstruction");

        // Verify slot positions: item input must be at col 0, row 0 (top-left)
        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Slot firstInputSlot = form.slots().get(0);
        assertEquals(dev.tysontheember.kubejsstudio.core.op.RecipeForm.Role.INPUT, firstInputSlot.role());
        assertEquals(0, firstInputSlot.col());
        assertEquals(0, firstInputSlot.row());

        // Parse JSON and verify every single value is extracted without null, empty, or ~
        dev.tysontheember.kubejsstudio.core.op.RecipeForm.Parsed parsed = form.parse(json);
        assertNotNull(parsed);

        assertTrue(parsed.slots().contains("astral_mekanism:coodinate_alloy"));
        assertTrue(parsed.slots().contains("astral_mekanism:wisdom_rivulet"));
        assertTrue(parsed.slots().contains("astral_mekanism:interstellar_antimatter"));
        assertTrue(parsed.slots().contains("evolvedmekanism:alloy_creative"));
        assertTrue(parsed.slots().contains("mekanism:lithium"));

        // Verify fields
        assertEquals("256", parsed.fields().get("duration"));
        assertEquals("10000000000000", parsed.fields().get("energyRequired"));
    }
}
