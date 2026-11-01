package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * High-level definition for Mekanism gas recipes.
 * Handles both JSON serializing (for Datapack) and KubeJS script generation.
 */
public final class MekanismGasRecipeDefinition {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final MekanismGasRecipeType type;
    private final String itemInput;
    private final int itemInputCount;
    private final MekanismGasStack gasInput;
    private final String fluidInput;
    private final int fluidInputAmount;

    private final String itemOutput;
    private final int itemOutputCount;
    private final MekanismGasStack gasOutput;
    private final String fluidOutput;
    private final int fluidOutputAmount;

    private final int duration;
    private final double energyRequired;

    public MekanismGasRecipeDefinition(
            MekanismGasRecipeType type,
            String itemInput, int itemInputCount,
            MekanismGasStack gasInput,
            String fluidInput, int fluidInputAmount,
            String itemOutput, int itemOutputCount,
            MekanismGasStack gasOutput,
            String fluidOutput, int fluidOutputAmount,
            int duration, double energyRequired
    ) {
        this.type = Objects.requireNonNull(type);
        this.itemInput = itemInput != null ? itemInput : "";
        this.itemInputCount = Math.max(1, itemInputCount);
        this.gasInput = gasInput;
        this.fluidInput = fluidInput != null ? fluidInput : "";
        this.fluidInputAmount = fluidInputAmount;
        this.itemOutput = itemOutput != null ? itemOutput : "";
        this.itemOutputCount = Math.max(1, itemOutputCount);
        this.gasOutput = gasOutput;
        this.fluidOutput = fluidOutput != null ? fluidOutput : "";
        this.fluidOutputAmount = fluidOutputAmount;
        this.duration = duration;
        this.energyRequired = energyRequired;
    }

    public MekanismGasRecipeType type() { return type; }
    public String itemInput() { return itemInput; }
    public int itemInputCount() { return itemInputCount; }
    public MekanismGasStack gasInput() { return gasInput; }
    public String itemOutput() { return itemOutput; }
    public int itemOutputCount() { return itemOutputCount; }
    public MekanismGasStack gasOutput() { return gasOutput; }

    /**
     * Serializes to Mekanism Recipe Datapack JSON.
     */
    public String toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", type.typeId());

        switch (type) {
            case OXIDIZING -> {
                // Item -> Gas
                JsonObject in = new JsonObject();
                if (itemInput.startsWith("#")) in.addProperty("tag", itemInput.substring(1));
                else in.addProperty("item", itemInput);
                if (itemInputCount > 1) in.addProperty("amount", itemInputCount);
                o.add("input", in);

                if (gasOutput != null) {
                    JsonObject out = new JsonObject();
                    out.addProperty("gas", gasOutput.cleanId());
                    out.addProperty("amount", gasOutput.amount());
                    o.add("output", out);
                }
            }
            case CRYSTALLIZING -> {
                // Gas -> Item
                if (gasInput != null) {
                    JsonObject in = new JsonObject();
                    if (gasInput.isTag()) in.addProperty("gasTag", gasInput.cleanId());
                    else in.addProperty("gas", gasInput.cleanId());
                    in.addProperty("amount", gasInput.amount());
                    o.add("input", in);
                }
                JsonObject out = new JsonObject();
                out.addProperty("item", itemOutput);
                if (itemOutputCount > 1) out.addProperty("count", itemOutputCount);
                o.add("output", out);
            }
            case PURIFYING, INJECTING, COMPRESSING -> {
                // Item + Gas -> Item
                JsonObject inItem = new JsonObject();
                if (itemInput.startsWith("#")) inItem.addProperty("tag", itemInput.substring(1));
                else inItem.addProperty("item", itemInput);
                if (itemInputCount > 1) inItem.addProperty("amount", itemInputCount);
                o.add("itemInput", inItem);

                if (gasInput != null) {
                    JsonObject inGas = new JsonObject();
                    if (gasInput.isTag()) inGas.addProperty("gasTag", gasInput.cleanId());
                    else inGas.addProperty("gas", gasInput.cleanId());
                    inGas.addProperty("amount", gasInput.amount());
                    o.add("chemicalInput", inGas);
                }

                JsonObject out = new JsonObject();
                out.addProperty("item", itemOutput);
                if (itemOutputCount > 1) out.addProperty("count", itemOutputCount);
                o.add("output", out);
            }
            case DISSOLUTION -> {
                // Item + Gas -> Gas
                JsonObject inItem = new JsonObject();
                if (itemInput.startsWith("#")) inItem.addProperty("tag", itemInput.substring(1));
                else inItem.addProperty("item", itemInput);
                if (itemInputCount > 1) inItem.addProperty("amount", itemInputCount);
                o.add("itemInput", inItem);

                if (gasInput != null) {
                    JsonObject inGas = new JsonObject();
                    if (gasInput.isTag()) inGas.addProperty("gasTag", gasInput.cleanId());
                    else inGas.addProperty("gas", gasInput.cleanId());
                    inGas.addProperty("amount", gasInput.amount());
                    o.add("gasInput", inGas);
                }

                if (gasOutput != null) {
                    JsonObject outGas = new JsonObject();
                    outGas.addProperty("gas", gasOutput.cleanId());
                    outGas.addProperty("amount", gasOutput.amount());
                    o.add("output", outGas);
                }
            }
            case ROTARY -> {
                // Fluid <-> Gas
                if (!fluidInput.isBlank() && gasOutput != null) {
                    JsonObject fIn = new JsonObject();
                    fIn.addProperty("amount", Math.max(1, fluidInputAmount));
                    fIn.addProperty("fluid", fluidInput);
                    o.add("fluidInput", fIn);

                    JsonObject gOut = new JsonObject();
                    gOut.addProperty("gas", gasOutput.cleanId());
                    gOut.addProperty("amount", gasOutput.amount());
                    o.add("gasOutput", gOut);
                } else if (gasInput != null && !fluidOutput.isBlank()) {
                    JsonObject gIn = new JsonObject();
                    gIn.addProperty("gas", gasInput.cleanId());
                    gIn.addProperty("amount", gasInput.amount());
                    o.add("gasInput", gIn);

                    JsonObject fOut = new JsonObject();
                    fOut.addProperty("amount", Math.max(1, fluidOutputAmount));
                    fOut.addProperty("fluid", fluidOutput);
                    o.add("fluidOutput", fOut);
                }
            }
            case REACTION -> {
                // Item + Fluid + Gas -> Item + Gas
                JsonObject inItem = new JsonObject();
                if (itemInput.startsWith("#")) inItem.addProperty("tag", itemInput.substring(1));
                else inItem.addProperty("item", itemInput);
                if (itemInputCount > 1) inItem.addProperty("amount", itemInputCount);
                o.add("itemInput", inItem);

                if (!fluidInput.isBlank()) {
                    JsonObject fIn = new JsonObject();
                    fIn.addProperty("amount", Math.max(1, fluidInputAmount));
                    fIn.addProperty("fluid", fluidInput);
                    o.add("fluidInput", fIn);
                }

                if (gasInput != null) {
                    JsonObject gIn = new JsonObject();
                    if (gasInput.isTag()) gIn.addProperty("gasTag", gasInput.cleanId());
                    else gIn.addProperty("gas", gasInput.cleanId());
                    gIn.addProperty("amount", gasInput.amount());
                    o.add("gasInput", gIn);
                }

                if (!itemOutput.isBlank()) {
                    JsonObject outItem = new JsonObject();
                    outItem.addProperty("item", itemOutput);
                    if (itemOutputCount > 1) outItem.addProperty("count", itemOutputCount);
                    o.add("itemOutput", outItem);
                }

                if (gasOutput != null) {
                    JsonObject outGas = new JsonObject();
                    outGas.addProperty("gas", gasOutput.cleanId());
                    outGas.addProperty("amount", gasOutput.amount());
                    o.add("gasOutput", outGas);
                }

                if (duration > 0) o.addProperty("duration", duration);
                if (energyRequired > 0) o.addProperty("energyRequired", energyRequired);
            }
        }

        return PRETTY.toJson(o);
    }

    /**
     * Converts to KubeJS recipe script call.
     * Example:
     * event.recipes.mekanism.purifying('2x mekanism:purified_iron', '#forge:ores/iron', '1x mekanism:oxygen')
     */
    public String toKubeJsScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("event.recipes.mekanism.").append(type.shortName()).append("(");

        switch (type) {
            case OXIDIZING -> {
                String outArg = gasOutput != null ? "'" + gasOutput.toKubeJsArg() + "'" : "''";
                String inArg = (itemInputCount > 1 ? (itemInputCount + "x ") : "") + itemInput;
                sb.append(outArg).append(", '").append(inArg).append("'");
            }
            case CRYSTALLIZING -> {
                String outArg = (itemOutputCount > 1 ? (itemOutputCount + "x ") : "") + itemOutput;
                String inArg = gasInput != null ? "'" + gasInput.toKubeJsArg() + "'" : "''";
                sb.append("'").append(outArg).append("', ").append(inArg);
            }
            case PURIFYING, INJECTING, COMPRESSING -> {
                String outArg = (itemOutputCount > 1 ? (itemOutputCount + "x ") : "") + itemOutput;
                String inItemArg = (itemInputCount > 1 ? (itemInputCount + "x ") : "") + itemInput;
                String inGasArg = gasInput != null ? "'" + gasInput.toKubeJsArg() + "'" : "''";
                sb.append("'").append(outArg).append("', '").append(inItemArg).append("', ").append(inGasArg);
            }
            case DISSOLUTION -> {
                String outArg = gasOutput != null ? "'" + gasOutput.toKubeJsArg() + "'" : "''";
                String inItemArg = (itemInputCount > 1 ? (itemInputCount + "x ") : "") + itemInput;
                String inGasArg = gasInput != null ? "'" + gasInput.toKubeJsArg() + "'" : "''";
                sb.append(outArg).append(", '").append(inItemArg).append("', ").append(inGasArg);
            }
            case ROTARY -> {
                if (!fluidInput.isBlank() && gasOutput != null) {
                    sb.append("'").append(gasOutput.toKubeJsArg()).append("', '").append(fluidInputAmount).append("x ").append(fluidInput).append("'");
                } else if (gasInput != null && !fluidOutput.isBlank()) {
                    sb.append("'").append(fluidOutputAmount).append("x ").append(fluidOutput).append("', '").append(gasInput.toKubeJsArg()).append("'");
                }
            }
            case REACTION -> {
                String outItemArg = !itemOutput.isBlank() ? "'" + (itemOutputCount > 1 ? itemOutputCount + "x " : "") + itemOutput + "'" : "''";
                String outGasArg = gasOutput != null ? "'" + gasOutput.toKubeJsArg() + "'" : "''";
                String inItemArg = "'" + (itemInputCount > 1 ? itemInputCount + "x " : "") + itemInput + "'";
                String inFluidArg = "'" + Math.max(1, fluidInputAmount) + "x " + fluidInput + "'";
                String inGasArg = gasInput != null ? "'" + gasInput.toKubeJsArg() + "'" : "''";
                sb.append(outItemArg).append(", ").append(outGasArg).append(", ").append(inItemArg).append(", ").append(inFluidArg).append(", ").append(inGasArg);
                if (duration > 0 || energyRequired > 0) {
                    sb.append(", ").append(duration).append(", ").append(energyRequired);
                }
            }
        }

        sb.append(");");
        return sb.toString();
    }
}
